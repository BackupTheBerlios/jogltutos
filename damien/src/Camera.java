//Pour les fenêtres
import java.awt.*;
import java.awt.event.*;

//Pour le jogl
import net.java.games.jogl.*;
import net.java.games.jogl.util.GLUT;

/**
 * Caméra à base de quaternions
 * La camera bouge par la modification de son heading, pitch et bank
 * Ces termes anglais sont liés à l'aéronautique
 * heading = le cap
 * pitch   = le tangage
 * bank    = le roulis
 * C'est le modèle utilsé par la plupart des soft 3D (Lighwave, 3DSMax)
 *
 * Pour l'utilisateur :
 * UP : Avancer (ou mettre les gazs)
 * DOWN : Reculer (ou inverser les gazs)
 * LEFT : Tourner à gauche
 * RIGHT : Tourner à droite
 * Souris : Contrôle de la vue
 *
 * Pour le codeur :
 * dans init : -on crée un objet de type Camera
 *             -on l'ajoute en tant que KeyListener et MouseMotionListener de
 *              l'objet GLDrawable
 * dans display : -on appelle setPerspective(GL gl) juste après le glLoadIdentity
 *                et juste avant un glTranslatef(0.f,0.f,-5.f)
 *
 */
public strictfp class Camera implements KeyListener, MouseMotionListener {


    /**
     * Classe interne CamPoint
     * Représente un point par ses coordonnées cartésiennes
     */
    strictfp class CamPoint {
        public float x;
        public float y;
        public float z;

        CamPoint() {
            x = 0.0f;
            y = 0.0f;
            z = 0.0f;
        }
    }//FIN DE LA CLASSE CamPoint


    /**
     * Classe interne CamVector
     * Représente un vecteur
     */
    strictfp class CamVector {

        public float i;
        public float j;
        public float k;

        /** Constructeur*/
        CamVector() {
            i = 0.0f;
            j = 0.0f;
            k = 0.0f;
        }

        /**Pour multiplier un vecteur par un scalaire*/
        CamVector multiplierScalaire(float scalaire) {
            CamVector c = new CamVector();
            c.i = i * scalaire;
            c.j = j * scalaire;
            c.k = k * scalaire;
            return c;
        }
    }//FIN DE LA CLASSE CamVector


    /**
     * Classe interne CamQuaternion
     * Représente les Quaternions de manière assez simple
     * Les opérations comme le conjugué ou la norme ne sont pas implémentées
     */
    strictfp class CamQuaternion {

        private float w;
        private float x;
        private float y;
        private float z;

        /**Constructeur*/
        CamQuaternion() {
            w = 1.0f;
            x = 0.0f;
            y = 0.0f;
            z = 0.0f;
        }

        /**
         * Transforme un quaternion en un quaternion représentant une
         * rotation quelconque dans l'espace
         */
        void createFromAxisAngle(float x, float y, float z, float degrees) {

            // On convertit les degrés en radians
            float angle = (degrees / 180.0f) * (float)Math.PI;

            // On calcule sinus(theta/2) une seule fois pour optimiser
            float result = (float)Math.sin(angle / 2.0f);

            // On calcule la valeur de w = cosinus(theta / 2)
            this.w = (float)Math.cos(angle / 2.0f);

            // On calcule les coordonnées x y z du quaternion
            this.x = x * result;
            this.y = y * result;
            this.z = z * result;
            normaliser();
        }

        /**
         * Normalise le quaternion courant
         */
        void normaliser() {
            float norme = norme();
            w = w/norme;
            x = x/norme;
            y = y/norme;
            z = z/norme;
        }

        /**
         * Renvoie la norme du quaternion courant
         */
        float norme() {
            return w*w + x*x + y*y + z*z;
        }

        /**
         * Pour créer à partir d'un quaternion une matrice qui peut être utilisée
         * par OpenGL
         */
        float[] createMatrix() {
            float[] matrix = new float[16];

            // Première COLONNE
            matrix[0] = 1.0f - 2.0f * ( y*y + z*z);
            matrix[1] = 2.0f * ( x*y + z*w);
            matrix[2] = 2.0f * ( x*z - y*w);
            matrix[3] = 0.0f;

            // Seconde COLONNE
            matrix[4] = 2.0f * ( x*y - z*w);
            matrix[5] = 1.0f - 2.0f * ( x*x + z*z);
            matrix[6] = 2.0f * ( z*y + x*w);
            matrix[7] = 0.0f;

            // Troisième COLONNE
            matrix[8] = 2.0f * ( x*z + y*w);
            matrix[9] = 2.0f * ( y*z - x*w);
            matrix[10] = 1.0f - 2.0f * ( x*x + y*y);
            matrix[11] = 0.0f;

            // Quatrième COLONNE
            matrix[12] = 0.0f;
            matrix[13] = 0.0f;
            matrix[14] = 0.0f;
            matrix[15] = 1.0f;

            // matrix est une matrice 4x4 homogène qui peut être utilisée pour les
            // calculs avec les matrices OpenGL
            return matrix;
        }

        /**Pour multiplier 2 quaternions*/
        CamQuaternion multiplier(CamQuaternion q) {
            CamQuaternion resultat = new CamQuaternion();
            resultat.w = w * q.w - x * q.x - y * q.y - z * q.z;
            resultat.x = w * q.x + x * q.w + y * q.z - z * q.y;
            resultat.y = w * q.y + y * q.w + z * q.x - x * q.z;
            resultat.z = w * q.z + z * q.w + x * q.y - y * q.x;
            return resultat;
        }
    }//FIN DE LA CLASSE CamQuaternion




    //Coefficient maximum pour la variation du pitch
    public float maxPitchRate;

    //Coefficient maximum pour la variation du heading
    public float maxHeadingRate;

    //Coefficient maximum pour la variation de vitesse
    public float maxForwardVelocity;

    //Angle de rotation du heading
    public float headingDegrees;

    //Angle de rotation du pitch
    public float pitchDegrees;

    //Valeur de la vitesse
    public float forwardVelocity;

    //Quaternions pour effectuer les rotations
    CamQuaternion qHeading;
    CamQuaternion qPitch;

    //La position de la caméra
    CamPoint position;

    //Le vecteur de la direction de la camera.
    CamVector directionVector;

    //Un Robot est un objet java utilisé pour les démonstrations d'applications
    //Il permet d'imposer la position du curseur de la souris, de le déplacer,
    //de simuler une frappe au clavier...
    Robot robot;


    /**Constructeur de la classe Camera*/
    public Camera() {

        try {
            robot = new Robot();
        }
        catch (Exception e) {
            System.out.println("Echec à l'initialisation du robot");
        }

        qHeading = new CamQuaternion();
        qPitch = new CamQuaternion();

        position = new CamPoint();
        position.x = 0.0f;
        position.y = 0.0f;
        position.z = -5.0f;

        directionVector = new CamVector();

        maxPitchRate =          1.0f;
        maxHeadingRate =        1.0f;
        maxForwardVelocity =    1.0f;
        headingDegrees =        0.0f;
        pitchDegrees =          0.0f;
        forwardVelocity =       0.0f;
    }

    /**
     * Pour définir la perspective, c'est à dire la façon dont on voit le monde
     * C'est ainsi que l'on "déplace" la caméra
     */
    public void setPerspective(GL gl) {

        float[] matrice = new float[16];
        CamQuaternion q = new CamQuaternion();

        // On fabrique les quaternions représentant nos rotations
        qPitch.createFromAxisAngle(1.0f, 0.0f, 0.0f, pitchDegrees);
        qHeading.createFromAxisAngle(0.0f, 1.0f, 0.0f, headingDegrees);

        // On multiplie les rotations pitch et heading
        q = qPitch.multiplier(qHeading);
        matrice = q.createMatrix();

        // OpenGL définit notre nouvelle perspective du monde
        // XXX : Cette opération est beaucoup plus couteuse que des appels à
        // glRotatef et glTranslatef
        gl.glMultMatrixf(matrice);

        // On crée une matrice à partir du quaternion représentant le pitch
        matrice = qPitch.createMatrix();
        // Et on en extrait le vecteur j représentant la direction dans laquelle
        // on regarde
        directionVector.j = matrice[9];

        // On multiplie les rotations heading et pitch
        q = qHeading.multiplier(qPitch);

        // On fabrique la matrice pour pouvoir en extraire les vecteurs i et k
        matrice = q.createMatrix();

        directionVector.i = matrice[8];
        directionVector.k = matrice[10];

        // On adapte le vecteur direction en fonction de la vitesse
        directionVector = directionVector.multiplierScalaire(forwardVelocity);

        //XXX
        // Remise à zéro de la vitesse :
        // - Cette classe représente à l'origine une caméra à la Wing Commander,
        // c'est à dire que l'on dirige la vue à la souris, et une impulsion
        // au clavier active les gazs, on avance alors tout seul (comme un
        // avion)
        // - Pour une caméra a la quake, ne pas appuyer sur le clavier
        // correspond à ne pas avancer
        //
        // Remettre cette variable à zéro en permanence est un pure bricolage
        // pour obtenir un comportement quake-like d'une caméra Wing Commander
        // De toute façon, elle reste "non straffante"
        forwardVelocity = 0.0f;
        //XXX

        // On incrémente la position à l'aide du vecteur
        position.x += directionVector.i;
        position.y += directionVector.j;
        position.z += directionVector.k;

        // On fait une translation jusque notre nouvelle position
        gl.glTranslatef( -position.x , -position.y , position.z);
    }

    /**Pour changer la direction*/
    void changePitch(float degrees) {

        // Notre pitch vaut moins que le maximum rate
        // donc on peut l'incrémenter
        if (Math.abs(degrees) < Math.abs(maxPitchRate)) {
            pitchDegrees += degrees;
        }

        // Sinon notre pitch vaut plus que le maximum rate
        // donc on peut seulement incrémenter le pitch
        // de ce maximum rate
        else {
            if (degrees < 0.0f) {
                pitchDegrees -= maxPitchRate;
            }
            else {
                pitchDegrees += maxPitchRate;
            }
        }

        // On ne veut pas que notre pitch "explose"
        if (pitchDegrees > 360.0f) {
            pitchDegrees -= 360.0f;
        }
        else {
            if (pitchDegrees < -360.0f) {
                pitchDegrees += 360.0f;
            }
        }
    }

    /**Pour changer le cap*/
    void changeHeading(float degrees) {

        // Notre heading vaut moins que le maximum rate
        // donc on peut l'incrémenter
        // MAIS on doit vérifier si nous sommes renversés
        if (Math.abs(degrees) < Math.abs(maxHeadingRate)) {
            if ( (pitchDegrees > 90.0f && pitchDegrees < 270.0f)
                    ||
                 (pitchDegrees < -90.0f && pitchDegrees > -270.0f)) {
                headingDegrees -= degrees;
            }
            else {
                headingDegrees += degrees;
            }
        }

        // Notre heading est plus grand que le maximum rate
        // donc on peut seulement incrémenter le heading
        // de ce maximum rate
        else {
            if (degrees < 0.0f) {
                if ( (pitchDegrees > 90.0f && pitchDegrees < 270.0f)
                        ||
                     (pitchDegrees < -90.0f && pitchDegrees > -270.0f)) {

                    // Normalement ici on décremente, mais vu que nous sommes
                    // renversés, on incrémente
                    headingDegrees += maxHeadingRate;
                }
                else {
                    headingDegrees -= maxHeadingRate;
                }
            }
            else {
                if ( (pitchDegrees > 90.0f && pitchDegrees < 270.0f)
                        ||
                     (pitchDegrees < -90.0f && pitchDegrees > -270.0f)) {
                    headingDegrees -= maxHeadingRate;
                }
                else {
                    headingDegrees += maxHeadingRate;
                }
            }
        }

        // On ne veut pas que notre heading "explose"
        if (headingDegrees > 360.0f) {
            headingDegrees -= 360.0f;
        }
        else {
            if (headingDegrees < -360.0f) {
                headingDegrees += 360.0f;
            }
        }
    }

    /**Pour changer la vitesse*/
    void changeVelocity(float vel) {

        // Notre vitesse vaut moins que le maximum rate
        // donc on peut l'incrémenter
        if (Math.abs(vel) < Math.abs(maxForwardVelocity)) {
            forwardVelocity += vel;
        }

        // Notre vitesse est plus grande que le maximum rate
        // donc on peut seulement l'incrémenter de ce maximum rate
        else {
            if (vel < 0.0f) {
                forwardVelocity -= -maxForwardVelocity;
            }
            else {
                forwardVelocity += maxForwardVelocity;
            }
        }

    }




    // ON IMPLEMENTE LE KEYLISTENER
    public void keyPressed(KeyEvent e) {
        //Quitter
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }

        //On accelère
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            changeVelocity(0.05f);
        }

        //On ralentit
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            changeVelocity(-0.05f);
        }

        //On tourne à gauche (on ne straffe pas)
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            changeHeading(-5.0f);
        }

        //On tourne à droite (on ne straffe pas)
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            changeHeading(5.0f);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {}


    // ON IMPLEMENTE LE MOUSEMOTIONLISTENER
    public void mouseDragged(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {

        //deltaMouse mesure de combien le curseur de souris s'éloigne du centre
        //de l'écran
        float deltaMouse = 0.0f;

        //On récupère le component qui communique avec le listener car on a
        //besoin de connaître les dimensions de la fenêtre
        Component component = e.getComponent();

        //On récupère le container du component (ie la Frame) car on a besoin de
        //récupérer les dimensions de la bordure (les Insets)
        //En effet les méthodes récupérant les coordonnées du curseur de souris
        //calculent à partir de l'origine de la fenêtre AVEC bordures
        Container container = component.getParent();

        //On récupère les informations sur les dimensions des bordures
        Insets insets = container.getInsets();

        //On récupère la taille de la fenêtre et on calcule le milieu
        Dimension size = component.getSize();

        //On cherche le centre de la zone opengl :
        //Coordonnée extérieure gauche de la frame + épaisseur de la bordure = on est
        //au bord gauche de la zone opengl
        // + la largeur de la zone opengl / 2  = le milieu de la zone opengl
        int centerX = container.getX() + insets.left + (component.getWidth() / 2);
        int centerY = container.getY() + insets.top + (component.getHeight() / 2);


        //e.getPoint().getX() = coordonnée x de la souris par rapport à la Frame
        // Donc on rajoute la coordonnée extérieur gauche de la frame + la
        // bordure

        // XXX
        // Explication du +1.0f : un nombre empirique
        // Selon le gestionnaire de fenêtre :
        //
        // Pour Windows XP / KDE3 / Fvwm2 :
        // - si l'origine de la fenêtre est en (1,1) il y a une différence de 1
        // pixel entre le calcul du centre et celui des coordonnées de la souris
        // Donc on ajoute 1
        // - si la fenêtre est n'importe où ailleurs on ne touche à rien
        //
        // Pour Fluxbox :
        // On constate sur Fluxbox (un window manager de Linux) un comportement
        // particulier.
        // - si l'origine de la fenêtre est en (1,1) il ne faut rien rajouter
        // - si la fenêtre est n'importe où ailleurs : il y a une différence de 1 pixel
        // entre le calcul du centre et celui des coordonnées de la souris
        // Donc on ajoute 1

        //CODE WINDOWS_XP KDE3 FVWM2
        /*
        float x = (float)(container.getX() +e.getPoint().getX() + insets.left);
        float y = (float)(container.getY() +e.getPoint().getY() + insets.top);

        if (container.getX() == 1 && container.getY() == 1) {
            x += 1.0f;
            y += 1.0f;
        }
        */
        //FIN CODE WINDOWS_XP KDE3 FVWM2

        //CODE FLUXBOX
        float x = (float)(container.getX() +e.getPoint().getX() + insets.left);
        float y = (float)(container.getY() +e.getPoint().getY() + insets.top);

        if (container.getX() != 1 || container.getY() != 1) {
            x += 1.0f;
            y += 1.0f;
        }
        //FIN CODE FLUXBOX


        if (x < centerX) {
            deltaMouse = (float)(centerX - x);
            changeHeading(-0.2f * deltaMouse);
        }
        else if (x > centerX) {
            deltaMouse = (float)(x - centerX);
            changeHeading(0.2f * deltaMouse);
        }

        if (y < centerY) {
            deltaMouse = (float)(centerY - y);
            changePitch(-0.2f * deltaMouse);
        }
        else if (y > centerY) {
            deltaMouse = (float)(y - centerY);
            changePitch(0.2f * deltaMouse);
        }

        //Le curseur de souris doit rester au centre de l'écran
        robot.mouseMove(centerX, centerY);
    }
}

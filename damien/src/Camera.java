//Pour les fen�tres
import java.awt.*;
import java.awt.event.*;

//Pour le jogl
import net.java.games.jogl.*;
import net.java.games.jogl.util.GLUT;

/**
 * Cam�ra � base de quaternions
 * La camera bouge par la modification de son heading, pitch et bank
 * Ces termes anglais sont li�s � l'a�ronautique
 * heading = le cap
 * pitch   = le tangage
 * bank    = le roulis
 * C'est le mod�le utils� par la plupart des soft 3D (Lighwave, 3DSMax)
 *
 * Pour l'utilisateur :
 * UP : Avancer (ou mettre les gazs)
 * DOWN : Reculer (ou inverser les gazs)
 * LEFT : Tourner � gauche
 * RIGHT : Tourner � droite
 * Souris : Contr�le de la vue
 *
 * Pour le codeur :
 * dans init : -on cr�e un objet de type Camera
 *             -on l'ajoute en tant que KeyListener et MouseMotionListener de
 *              l'objet GLDrawable
 * dans display : -on appelle setPerspective(GL gl) juste apr�s le glLoadIdentity
 *                et juste avant un glTranslatef(0.f,0.f,-5.f)
 *
 */
public strictfp class Camera implements KeyListener, MouseMotionListener {


    /**
     * Classe interne CamPoint
     * Repr�sente un point par ses coordonn�es cart�siennes
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
     * Repr�sente un vecteur
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
     * Repr�sente les Quaternions de mani�re assez simple
     * Les op�rations comme le conjugu� ou la norme ne sont pas impl�ment�es
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
         * Transforme un quaternion en un quaternion repr�sentant une
         * rotation quelconque dans l'espace
         */
        void createFromAxisAngle(float x, float y, float z, float degrees) {

            // On convertit les degr�s en radians
            float angle = (degrees / 180.0f) * (float)Math.PI;

            // On calcule sinus(theta/2) une seule fois pour optimiser
            float result = (float)Math.sin(angle / 2.0f);

            // On calcule la valeur de w = cosinus(theta / 2)
            this.w = (float)Math.cos(angle / 2.0f);

            // On calcule les coordonn�es x y z du quaternion
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
         * Pour cr�er � partir d'un quaternion une matrice qui peut �tre utilis�e
         * par OpenGL
         */
        float[] createMatrix() {
            float[] matrix = new float[16];

            // Premi�re COLONNE
            matrix[0] = 1.0f - 2.0f * ( y*y + z*z);
            matrix[1] = 2.0f * ( x*y + z*w);
            matrix[2] = 2.0f * ( x*z - y*w);
            matrix[3] = 0.0f;

            // Seconde COLONNE
            matrix[4] = 2.0f * ( x*y - z*w);
            matrix[5] = 1.0f - 2.0f * ( x*x + z*z);
            matrix[6] = 2.0f * ( z*y + x*w);
            matrix[7] = 0.0f;

            // Troisi�me COLONNE
            matrix[8] = 2.0f * ( x*z + y*w);
            matrix[9] = 2.0f * ( y*z - x*w);
            matrix[10] = 1.0f - 2.0f * ( x*x + y*y);
            matrix[11] = 0.0f;

            // Quatri�me COLONNE
            matrix[12] = 0.0f;
            matrix[13] = 0.0f;
            matrix[14] = 0.0f;
            matrix[15] = 1.0f;

            // matrix est une matrice 4x4 homog�ne qui peut �tre utilis�e pour les
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

    //La position de la cam�ra
    CamPoint position;

    //Le vecteur de la direction de la camera.
    CamVector directionVector;

    //Un Robot est un objet java utilis� pour les d�monstrations d'applications
    //Il permet d'imposer la position du curseur de la souris, de le d�placer,
    //de simuler une frappe au clavier...
    Robot robot;


    /**Constructeur de la classe Camera*/
    public Camera() {

        try {
            robot = new Robot();
        }
        catch (Exception e) {
            System.out.println("Echec � l'initialisation du robot");
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
     * Pour d�finir la perspective, c'est � dire la fa�on dont on voit le monde
     * C'est ainsi que l'on "d�place" la cam�ra
     */
    public void setPerspective(GL gl) {

        float[] matrice = new float[16];
        CamQuaternion q = new CamQuaternion();

        // On fabrique les quaternions repr�sentant nos rotations
        qPitch.createFromAxisAngle(1.0f, 0.0f, 0.0f, pitchDegrees);
        qHeading.createFromAxisAngle(0.0f, 1.0f, 0.0f, headingDegrees);

        // On multiplie les rotations pitch et heading
        q = qPitch.multiplier(qHeading);
        matrice = q.createMatrix();

        // OpenGL d�finit notre nouvelle perspective du monde
        // XXX : Cette op�ration est beaucoup plus couteuse que des appels �
        // glRotatef et glTranslatef
        gl.glMultMatrixf(matrice);

        // On cr�e une matrice � partir du quaternion repr�sentant le pitch
        matrice = qPitch.createMatrix();
        // Et on en extrait le vecteur j repr�sentant la direction dans laquelle
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
        // Remise � z�ro de la vitesse :
        // - Cette classe repr�sente � l'origine une cam�ra � la Wing Commander,
        // c'est � dire que l'on dirige la vue � la souris, et une impulsion
        // au clavier active les gazs, on avance alors tout seul (comme un
        // avion)
        // - Pour une cam�ra a la quake, ne pas appuyer sur le clavier
        // correspond � ne pas avancer
        //
        // Remettre cette variable � z�ro en permanence est un pure bricolage
        // pour obtenir un comportement quake-like d'une cam�ra Wing Commander
        // De toute fa�on, elle reste "non straffante"
        forwardVelocity = 0.0f;
        //XXX

        // On incr�mente la position � l'aide du vecteur
        position.x += directionVector.i;
        position.y += directionVector.j;
        position.z += directionVector.k;

        // On fait une translation jusque notre nouvelle position
        gl.glTranslatef( -position.x , -position.y , position.z);
    }

    /**Pour changer la direction*/
    void changePitch(float degrees) {

        // Notre pitch vaut moins que le maximum rate
        // donc on peut l'incr�menter
        if (Math.abs(degrees) < Math.abs(maxPitchRate)) {
            pitchDegrees += degrees;
        }

        // Sinon notre pitch vaut plus que le maximum rate
        // donc on peut seulement incr�menter le pitch
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
        // donc on peut l'incr�menter
        // MAIS on doit v�rifier si nous sommes renvers�s
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
        // donc on peut seulement incr�menter le heading
        // de ce maximum rate
        else {
            if (degrees < 0.0f) {
                if ( (pitchDegrees > 90.0f && pitchDegrees < 270.0f)
                        ||
                     (pitchDegrees < -90.0f && pitchDegrees > -270.0f)) {

                    // Normalement ici on d�cremente, mais vu que nous sommes
                    // renvers�s, on incr�mente
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
        // donc on peut l'incr�menter
        if (Math.abs(vel) < Math.abs(maxForwardVelocity)) {
            forwardVelocity += vel;
        }

        // Notre vitesse est plus grande que le maximum rate
        // donc on peut seulement l'incr�menter de ce maximum rate
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

        //On accel�re
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            changeVelocity(0.05f);
        }

        //On ralentit
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            changeVelocity(-0.05f);
        }

        //On tourne � gauche (on ne straffe pas)
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            changeHeading(-5.0f);
        }

        //On tourne � droite (on ne straffe pas)
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

        //deltaMouse mesure de combien le curseur de souris s'�loigne du centre
        //de l'�cran
        float deltaMouse = 0.0f;

        //On r�cup�re le component qui communique avec le listener car on a
        //besoin de conna�tre les dimensions de la fen�tre
        Component component = e.getComponent();

        //On r�cup�re le container du component (ie la Frame) car on a besoin de
        //r�cup�rer les dimensions de la bordure (les Insets)
        //En effet les m�thodes r�cup�rant les coordonn�es du curseur de souris
        //calculent � partir de l'origine de la fen�tre AVEC bordures
        Container container = component.getParent();

        //On r�cup�re les informations sur les dimensions des bordures
        Insets insets = container.getInsets();

        //On r�cup�re la taille de la fen�tre et on calcule le milieu
        Dimension size = component.getSize();

        //On cherche le centre de la zone opengl :
        //Coordonn�e ext�rieure gauche de la frame + �paisseur de la bordure = on est
        //au bord gauche de la zone opengl
        // + la largeur de la zone opengl / 2  = le milieu de la zone opengl
        int centerX = container.getX() + insets.left + (component.getWidth() / 2);
        int centerY = container.getY() + insets.top + (component.getHeight() / 2);


        //e.getPoint().getX() = coordonn�e x de la souris par rapport � la Frame
        // Donc on rajoute la coordonn�e ext�rieur gauche de la frame + la
        // bordure

        // XXX
        // Explication du +1.0f : un nombre empirique
        // Selon le gestionnaire de fen�tre :
        //
        // Pour Windows XP / KDE3 / Fvwm2 :
        // - si l'origine de la fen�tre est en (1,1) il y a une diff�rence de 1
        // pixel entre le calcul du centre et celui des coordonn�es de la souris
        // Donc on ajoute 1
        // - si la fen�tre est n'importe o� ailleurs on ne touche � rien
        //
        // Pour Fluxbox :
        // On constate sur Fluxbox (un window manager de Linux) un comportement
        // particulier.
        // - si l'origine de la fen�tre est en (1,1) il ne faut rien rajouter
        // - si la fen�tre est n'importe o� ailleurs : il y a une diff�rence de 1 pixel
        // entre le calcul du centre et celui des coordonn�es de la souris
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

        //Le curseur de souris doit rester au centre de l'�cran
        robot.mouseMove(centerX, centerY);
    }
}

import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.util.*;
import net.java.games.jogl.*;

public class Encore {

    static Animator animator = null;

    static CamQuaternion r1,r2;
    static class Renderer implements GLEventListener,
            KeyListener, MouseListener, MouseMotionListener {

        /* Caractéristiques de la rotation courante
           vecteur de coordonnées (rx, ry, rz)
           angle de rotation : alpha
         */
        float rx, ry, rz, alpha;

        /* Coordonnées du point au moment du click : (cx, cy)
           Coordonnées courantes du curseur : (nx, ny)
         */
        double nx, ny, cx, cy;

        float[] rmat = {1,0,0,0,
                       0,1,0,0,
                       0,0,1,0,
                       0,0,0,1};
        float[] matrice = {1,0,0,0,
                       0,1,0,0,
                       0,0,1,0,
                       0,0,0,1};
        public void init(GLDrawable glDrawable) {
            final GL gl = glDrawable.getGL();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glEnable(gl.GL_LINE_SMOOTH);


            r1 = new CamQuaternion();
            r2 = new CamQuaternion();
            // Ecouteurs pour interactions utilisateur
            glDrawable.addKeyListener(this);
            glDrawable.addMouseMotionListener(this);
            glDrawable.addMouseListener(this);
        }
boolean trans = true;
        public void display(GLDrawable gLDrawable) {

            final GL gl = gLDrawable.getGL();
            GLUT glut = new GLUT();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            gl.glLoadIdentity();

            //if (trans){

                //trans = false;
            //}
            //
            gl.glPushMatrix();
            // Effectue la rotation courante
            //gl.glRotated(alpha, rx, ry, rz);
            gl.glTranslatef(0, 0, -15);
            gl.glMultMatrixf(rmat);
            //gl.glPopMatrix();
            /*for (int i = 0; i < 16; i++)
                System.out.print(rmat[i] + " ");
            System.out.println("");*/
    gl.glGetFloatv(gl.GL_MODELVIEW_MATRIX,matrice);
            for (int i = 0; i < 16; i++) System.out.print(matrice[i] + "Av ");
            System.out.println("");

            // Dessine le cube
            gl.glColor3f(0, 0, 1);
            glut.glutSolidCube(gl, 8);
            gl.glColor3f(1f, 0.2f, 0.2f);
            glut.glutWireCube(gl, 8);// Les contours
            gl.glPopMatrix();

        }


        public void mousePressed(MouseEvent e) {
            // Enregistre la position du curseur au moment du clic
            cx = e.getX();
            cy = e.getY();
            /*for (int i = 0; i < 4; i++)
                if (i%5 == 0){
                    rmat[i] = 1;
                    matrice[1] = 1;
                }
                else{
                    rmat[i] = matrice[1] = 0;
                }*/
            matrice = rmat;
        }

        public void mouseReleased(MouseEvent e) {


        }


        public void mouseDragged(MouseEvent e) {
            /* Déplacement de la souris
               C'est dans cette méthode que l'on va faire les
               calculs pour la boule virtuelle
             */
            matrice = rmat;
            // Nouvelle position du curseur
            nx = e.getX();
            ny = e.getY();

            // Taille de la fenêtre
            int W = e.getComponent().getWidth();
            int H = e.getComponent().getHeight();

            // Le rayon de la boule virtuelle
            double r = Math.min(W / 2, H / 2);

            /* On va calculer deux vecteurs U(ux, uy, uz) et V(vx, vy, vz)
               U correspond au vecteur qui part de l'origine et passe par le
               point au moment du clic
               V correspond au vecteur qui part de l'origine et passe par la
               position du curseur
             */
            float ux, uy, uz, vx, vy, vz;

            /* Conversion entre repère écran et repère de la boule virtuelle
               Il s'agit d'un simple décalage pour centrer le repère sur l'écran
               Le signe négatif des coordonnées x dépendent du repère choisi
             */
           ux = (float)(-(nx - W / 2));
           uy = (float)(ny - H / 2);
           vx = (float)(-(cx - W / 2));
           vy = (float)(cy - H / 2);

           // Coordonnées Z de U
           if ((ux * ux + uy * uy) < r * r) // Dans la sphère
               uz = (float)(- Math.sqrt(r * r - ux * ux - uy * uy));
           else uz = 0; // Hors de la sphère

           // Coordonnées Z de V
           if ((vx * vx + vy * vy) < r * r) // Dans la sphère
               vz = (float)(- Math.sqrt(r * r - vx * vx - vy * vy));
           else vz = 0; // Hors de la sphère

           // On a maintenant les vecteurs U et V, on peut calculer leurs normes
            double normeU = Math.sqrt(vx * vx + vy * vy + vz * vz);
            double normeV = Math.sqrt(ux * ux + uy * uy + uz * uz);

            // Puis leur produit scalaire
            double ps = vx * ux + vy * uy + vz * uz;

            // Pour en déduire l'angle de rotation en degré
            alpha = (float)(Math.acos(ps/(normeU*normeV)) * 180 / Math.PI);

            /* Il reste maintenant à définir l'axe de rotation
               On calcul le produit vectoriel de U et V et on obtient
               l'axe de rotation sous la forme du vecteur (rx, ry, rz)
             */
            rx = vy * uz - vz * uy;
            ry = vz * ux - vx * uz;
            rz = vx * uy - vy * ux;

            r1.createFromAxisAngle(rx, ry, rz, alpha);

            //r1.normaliser();

            rmat = multiplyMatrix(matrice,r1.createMatrix());
            /*for (int i = 0; i < 4; i++)
                if (i%5 == 0){
                    rmat[i] = 1;
                    //matrice[i] = 1;
                }
                else{
                    rmat[i] = 0;
                    //matrice[i] = 0;
                }*/

            //r1.createFromAxisAngle(0,0,0,0);
            /* Les variables de rotation sont maintenant toutes définies et
               seront appellées dans la méthode display
            */
        }

        public float[] multiplyMatrix(float[] a, float[] b) {
            float[] ret = new float[16];
            for(int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    ret[i * 4 + j] = 0;
                    for (int k = 0; k < 4; k++) {
                        ret[i * 4 + j] += b[i * 4 + k] * a[k * 4 + j];
                    }
                }
            }
            return ret;
    }
        public void keyPressed(KeyEvent e) {
            // Fermeture de la fenêtre sur touche Escape
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                animator.stop();
                System.exit(0);
            }
        }

        public void reshape(GLDrawable gLDrawable, int x, int y, int width,
                            int height) {
            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();

            if (height <= 0) { // avoid a divide by zero error!
                height = 1;
            }
            final float h = (float) width / (float) height;
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glLoadIdentity();

            // Définition de la caméra
            glu.gluPerspective(65.0f, h, 1.0, 2000.0);

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();

        }

        public void displayChanged(GLDrawable gLDrawable, boolean modeChanged,
                                   boolean deviceChanged) {}

        public void keyReleased(KeyEvent e) {}

        public void keyTyped(KeyEvent e) {}

        public void mouseClicked(MouseEvent e) {}

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

        public void mouseMoved(MouseEvent e) {}

    }
        /**
             * Classe interne CamQuaternion
             * Représente les Quaternions de manière assez simple
             * Les opérations comme le conjugué ou la norme ne sont pas implémentées
             */
            static strictfp class CamQuaternion {

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
                    float norme = (float)Math.sqrt(norme());
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



    public static void main(String[] args) {
        Frame frame = new Frame("Boule virtuelle");
        GLCanvas canvas = GLDrawableFactory.getFactory().createGLCanvas(new
                GLCapabilities());
        canvas.addGLEventListener(new Renderer());
        frame.add(canvas);

        frame.setSize(640, 480);
        animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                animator.stop();
                System.exit(0);
            }
        });
        frame.show();
        animator.start();

        canvas.requestFocus();
    }
}

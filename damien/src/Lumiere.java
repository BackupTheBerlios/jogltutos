import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.util.*;
import net.java.games.jogl.*;

public class Lumiere {

    static Animator animator = null;


    static class Renderer implements GLEventListener,
            KeyListener, MouseListener, MouseMotionListener {

        /* Caract�ristiques de la rotation courante
           vecteur de coordonn�es (rx, ry, rz)
           angle de rotation : alpha
         */
        double rx, ry, rz, alpha;

        /* Coordonn�es du point au moment du click : (cx, cy)
           Coordonn�es courantes du curseur : (nx, ny)
         */
        double nx, ny, cx, cy;

        public void init(GLDrawable glDrawable) {
            final GL gl = glDrawable.getGL();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glEnable(gl.GL_LINE_SMOOTH);
            gl.glShadeModel(gl.GL_SMOOTH);

            gl.glEnable(gl.GL_LIGHTING); // Active la gestion des lumi�res
            gl.glEnable(gl.GL_LIGHT0);

            float[] bleu = {0.0f, 0.0f, 1.0f, 1.0f};
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT, bleu);
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, bleu);
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, bleu);

            // Ecouteurs pour interactions utilisateur
            glDrawable.addKeyListener(this);
            glDrawable.addMouseMotionListener(this);
            glDrawable.addMouseListener(this);
        }

        float rot = -2;
            boolean signe;
        public void display(GLDrawable gLDrawable) {


            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();
            GLUT glut = new GLUT();

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            // Positionne la cam�ra

            float light_position[] = {lx, ly, lz, 1};

            gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, light_position);
            //System.out.println(lx + " "+ ly+" " + lz);
            gl.glTranslatef(0, 0, -20);

            // Effectue la rotation courante
            gl.glRotated(alpha, rx, ry, rz);

            // Dessine le cube
            gl.glPushMatrix();
            gl.glTranslated(lx,ly,lz);
            glut.glutSolidSphere(glu, 1,20,20);
            gl.glPopMatrix();

            glut.glutSolidCube(gl, 8);
            gl.glColor3f(1f, 0.2f, 0.2f);
            glut.glutWireCube(gl, 8); // Les contours
        }
        boolean button;
        float lx, ly, lz;
        public void mousePressed(MouseEvent e) {
            // Enregistre la position du curseur au moment du clic
             if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
                 cx = e.getX();
                 cy = e.getY();
             }
             else {
                 cx = e.getX();
                 cy = e.getY();
                 button = true;
             }
        }

        public void mouseReleased(MouseEvent e) {
            button = !false;
        }


        public void mouseDragged(MouseEvent e) {
            /* D�placement de la souris
               C'est dans cette m�thode que l'on va faire les
               calculs pour la boule virtuelle
             */

            // Nouvelle position du curseur
            nx = e.getX();
            ny = e.getY();

            // Taille de la fen�tre
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
            double ux, uy, uz, vx, vy, vz;

            /* Conversion entre rep�re �cran et rep�re de la boule virtuelle
             Il s'agit d'un simple d�calage pour centrer le rep�re sur l'�cran
               Le signe n�gatif des coordonn�es x d�pendent du rep�re choisi
             */
            ux = -(nx - W / 2);
            uy = ny - H / 2;
            vx = -(cx - W / 2);
            vy = cy - H / 2;

            // Coordonn�es Z de U
            if ((ux * ux + uy * uy) < r * r) { // Dans la sph�re
                uz = -Math.sqrt(r * r - ux * ux - uy * uy);
            } else {
                uz = 0; // Hors de la sph�re
            }

            // Coordonn�es Z de V
            if ((vx * vx + vy * vy) < r * r) { // Dans la sph�re
                vz = -Math.sqrt(r * r - vx * vx - vy * vy);
            } else {
                vz = 0; // Hors de la sph�re
            }


            // On a maintenant les vecteurs U et V, on peut calculer leurs normes
            double normeU = Math.sqrt(vx * vx + vy * vy + vz * vz);
            double normeV = Math.sqrt(ux * ux + uy * uy + uz * uz);

            if (button){
                System.out.println("here" + vx + " " + vy);
                lx = -(float)ux/30;////(float)normeV;
                ly = -(float)uy/30;//(float)normeV;
                lz = Math.abs((float)uz/30);//(float)normeV;
                return;
            }
            // Puis leur produit scalaire
            double ps = vx * ux + vy * uy + vz * uz;

            // Pour en d�duire l'angle de rotation en degr�
            alpha = Math.acos(ps / (normeU * normeV)) * 180 / Math.PI;

            /* Il reste maintenant � d�finir l'axe de rotation
               On calcul le produit vectoriel de U et V et on obtient
               l'axe de rotation sous la forme du vecteur (rx, ry, rz)
             */
            rx = vy * uz - vz * uy;
            ry = vz * ux - vx * uz;
            rz = vx * uy - vy * ux;

            /* Les variables de rotation sont maintenant toutes d�finies et
               seront appell�es dans la m�thode display
             */
        }

        public void keyPressed(KeyEvent e) {
            // Fermeture de la fen�tre sur touche Escape
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

            // D�finition de la cam�ra
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

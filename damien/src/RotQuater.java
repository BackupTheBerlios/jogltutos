import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.util.*;
import net.java.games.jogl.*;

public class RotQuater {

    static Animator animator = null;

    static CamQuaternion r1, r2, r3;
    static class Renderer implements GLEventListener,
            KeyListener, MouseListener, MouseMotionListener {

        public Renderer() {
            //r1 = new CamQuaternion();
        }

        /* Caractéristiques de la rotation courante
           vecteur de coordonnées (rx, ry, rz)
           angle de rotation : alpha
         */
        double rx, ry, rz, alpha;
        double sx, sy, sz, salpha;


        /* Coordonnées du point au moment du click : (cx, cy)
           Coordonnées courantes du curseur : (nx, ny)
         */
        double nx, ny, cx, cy;

        public void init(GLDrawable glDrawable) {

            final GL gl = glDrawable.getGL();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glEnable(gl.GL_LINE_SMOOTH);
            r1 = new CamQuaternion();
            r2 = new CamQuaternion();
            r3 = new CamQuaternion();

            //System.out.println(r1.x + " " + r2.x);
            // Ecouteurs pour interactions utilisateur
            glDrawable.addKeyListener(this);
            glDrawable.addMouseMotionListener(this);
            glDrawable.addMouseListener(this);
        }

        public void display(GLDrawable gLDrawable) {

            final GL gl = gLDrawable.getGL();
            GLUT glut = new GLUT();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            // Positionne la caméra

            gl.glTranslatef(0, 0, -20);

            // Effectue la rotation courante
            gl.glRotated(alpha, rx, ry, rz);

            gl.glRotated(salpha, sx, sy, sz);
            gl.glPushMatrix();
            //gl.glMultMatrixf(rr);
            System.out.println("S " + salpha + " " + sx + " " + sy + " " + sz);
            System.out.println("R " + alpha + " " + rx + " " + ry + " " + rz);
            //for (int i = 0; i < 16; i++) System.out.println(r2.createMatrix()[i] + " " );
            //gl.glMultMatrixf(r2.createMatrix());
            // Dessine le cube
            gl.glColor3f(0, 0, 1);
            glut.glutSolidCube(gl, 8);
            gl.glColor3f(1f, 0.2f, 0.2f);
            glut.glutWireCube(gl, 8); // Les contours
            gl.glPopMatrix();
        }

        public void mousePressed(MouseEvent e) {
            // Enregistre la position du curseur au moment du clic
            cx = e.getX();
            cy = e.getY();
        }
float[] rr = {1,0,0,0,
             0,1,0,0,
             0,0,1,0,
             0,0,0,1};
        public void mouseReleased(MouseEvent e) {
            //r1.createFromAxisAngle((float)(sx), (float)(sy), (float)(sz),
             //                      (float) salpha);
            r2.createFromAxisAngle((float)(rx), (float)(ry), (float)(rz),
                                   (float) alpha);

            /*r1.normaliser();
            r2.normaliser();*/
            r3 = r2.multiplier(r1);
            //r3.normaliser();
            rr = r3.createMatrix();
            r1.setValues(r2);
            /*double[] tmp = r3.getRotation();
            salpha = tmp[0];
            sx = tmp[1];
            sy = tmp[2];
            sz = tmp[3];
            rx = ry = rz = alpha = 0;*/
            //r2.createFromAxisAngle(r,2,5,2);
            //r1.createFromAxisAngle(2,2,2,2);
        }


        public void mouseDragged(MouseEvent e) {
            /* Déplacement de la souris
               C'est dans cette méthode que l'on va faire les
               calculs pour la boule virtuelle
             */

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
            double ux, uy, uz, vx, vy, vz;

            /* Conversion entre repère écran et repère de la boule virtuelle
             Il s'agit d'un simple décalage pour centrer le repère sur l'écran
               Le signe négatif des coordonnées x dépendent du repère choisi
             */
            ux = -(nx - W / 2);
            uy = ny - H / 2;
            vx = -(cx - W / 2);
            vy = cy - H / 2;

            // Coordonnées Z de U
            if ((ux * ux + uy * uy) < r * r) { // Dans la sphère
                uz = -Math.sqrt(r * r - ux * ux - uy * uy);
            } else {
                uz = 0; // Hors de la sphère
            }

            // Coordonnées Z de V
            if ((vx * vx + vy * vy) < r * r) { // Dans la sphère
                vz = -Math.sqrt(r * r - vx * vx - vy * vy);
            } else {
                vz = 0; // Hors de la sphère
            }

            // On a maintenant les vecteurs U et V, on peut calculer leurs normes
            double normeU = Math.sqrt(vx * vx + vy * vy + vz * vz);
            double normeV = Math.sqrt(ux * ux + uy * uy + uz * uz);

            // Puis leur produit scalaire
            double ps = vx * ux + vy * uy + vz * uz;

            // Pour en déduire l'angle de rotation en degré
            alpha = Math.acos(ps / (normeU * normeV)) * 180 / Math.PI;

            /* Il reste maintenant à définir l'axe de rotation
               On calcul le produit vectoriel de U et V et on obtient
               l'axe de rotation sous la forme du vecteur (rx, ry, rz)
             */
            rx = vy * uz - vz * uy;
            ry = vz * ux - vx * uz;
            rz = vx * uy - vy * ux;

            /* Les variables de rotation sont maintenant toutes définies et
               seront appellées dans la méthode display
             */
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

import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.util.*;
import net.java.games.jogl.*;

public class Eclairage {
    public Eclairage() {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static Animator animator = null;

    static class Renderer implements GLEventListener,
            KeyListener, MouseListener, MouseMotionListener {

        float rot = -5;
        boolean signe;
        GLDrawable gld;
        public void display(GLDrawable gLDrawable) {
            gld = gLDrawable;
            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();
            GLUT glut = new GLUT();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            double r=10;
            float z = (signe?1f:-1f)*(float)Math.sqrt(r-rot*rot);
            if (z == 0)
                signe = !signe;
            float light_position[] = { rot, 0, z, 0 };
            rot += 0.1;
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, light_position);

            gl.glTranslatef(0, 0, -30);

            gl.glRotatef(30,0,1,1);
            gl.glRotatef(30,1,0,0);

            //glut.glutWireCube(gl, 8);
            //glut.glutSolidCube(gl,10);
            glut.glutSolidSphere(glu,5,20,20);
            //glut.glutWireSphere(glu,5,10,10);

            gl.glFlush();

        }

        public void displayChanged(GLDrawable gLDrawable, boolean modeChanged,
                                   boolean deviceChanged) {
        }

        public void init(GLDrawable gLDrawable) {
            final GL gl = gLDrawable.getGL();
            gl.glShadeModel(gl.GL_SMOOTH); // Enable Smooth Shading
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
            gl.glClearDepth(1.0f); // Depth Buffer Setup
            gl.glEnable(gl.GL_LINE_SMOOTH);
            gl.glEnable(gl.GL_LIGHTING) ;                 // Active la gestion des lumières
            gl.glEnable(gl.GL_LIGHT0) ;

            float[] bleu = { 0.0f, 0.0f, 1.0f, 1.0f };
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT, bleu);
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, bleu);
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, bleu);

            float light_ambient[] = { 0, 0, 0, 1 };


            float light_position[] = { 1, 1, 1, 0 };
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, light_position);

            gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT, light_ambient);

            gLDrawable.addKeyListener(this);
            gLDrawable.addMouseMotionListener(this);
            gLDrawable.addMouseListener(this);


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
            glu.gluPerspective(50.0f, h, 1.0, 2000.0);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();

        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                animator.stop();
                System.exit(0);

            }
        }

        public void keyReleased(KeyEvent e) {}

        /** Invoked when a key has been typed.
         * See the class description for {@link KeyEvent} for a definition of
         * a key typed event.
         * @param e The KeyEvent.
         */
        public void keyTyped(KeyEvent e) {}

        public void mouseClicked(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseDragged(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {

        }


    }

    public static void main(String[] args) {
        Frame frame = new Frame("Eclairage");
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

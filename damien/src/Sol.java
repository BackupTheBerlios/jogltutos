import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.util.*;
import net.java.games.jogl.*;

/** Port of the NeHe OpenGL Tutorial (Lesson 5)
 * to Java using the Jogl interface to OpenGL.  Jogl can be obtained
 * at http://jogl.dev.java.net/
 *
 * @author Kevin Duling (jattier@hotmail.com)
 */
public class Sol {
    public Sol() {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static Animator animator = null;

    static class Renderer implements GLEventListener,
            KeyListener {

        private float rtri = 0.0f;
        private float angle =  0;

        public void display(GLDrawable gLDrawable) {
            final GL gl = gLDrawable.getGL();

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            gl.glTranslatef( 0, 0f, -16);
            int L = 25; // Longueur de la surface
            int l = 25; // Largeur de la surface
            float t = .5f; // taille des triangles

            gl.glBegin(GL.GL_TRIANGLES);
            for (int i = -L / 2; i < L / 2; i++) {
                for (int j = -l / 2; j < l / 2; j++) {
                    // On trace un carré (2 triangles)
                    // Premier triangle
                    gl.glColor3f(1f, 0.2f, 0.2f);
                    gl.glVertex3f(i * t, j * t, 0);
                    gl.glVertex3f((i + 1) * t, j * t + t, 0);
                    gl.glVertex3f(i * t, j * t + t, 0);

                    // Deuxieme triangle
                    gl.glColor3f(0.2f, 0.2f, 1f);
                    gl.glVertex3f(i * t, j * t, 0);
                    gl.glVertex3f((i + 1) * t, j * t + t, 0);
                    gl.glVertex3f((i + 1) * t, j * t, 0);

                }
            }
            gl.glEnd();

            gl.glFlush();
            rtri += 0.1f;

        }


        /** Called when the display mode has been changed.  <B>!! CURRENTLY UNIMPLEMENTED IN JOGL !!</B>
         * @param gLDrawable The GLDrawable object.
         * @param modeChanged Indicates if the video mode has changed.
         * @param deviceChanged Indicates if the video device has changed.
         */
        public void displayChanged(GLDrawable gLDrawable, boolean modeChanged,
                                   boolean deviceChanged) {
        }

        /** Called by the drawable immediately after the OpenGL context is
         * initialized for the first time. Can be used to perform one-time OpenGL
         * initialization such as setup of lights and display lists.
         * @param gLDrawable The GLDrawable object.
         */
        public void init(GLDrawable gLDrawable) {
            final GL gl = gLDrawable.getGL();
            //gl.glShadeModel(gl.GL_FLAT); // Enable Smooth Shading
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
            gl.glClearDepth(1.0f); // Depth Buffer Setup
            gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // Really Nice Perspective Calculations
            gLDrawable.addKeyListener(this);
        }


        /** Called by the drawable during the first repaint after the component has
         * been resized. The client can update the viewport and view volume of the
         * window appropriately, for example by a call to
         * GL.glViewport(int, int, int, int); note that for convenience the component
         * has already called GL.glViewport(int, int, int, int)(x, y, width, height)
         * when this method is called, so the client may not have to do anything in
         * this method.
         * @param gLDrawable The GLDrawable object.
         * @param x The X Coordinate of the viewport rectangle.
         * @param y The Y coordinate of the viewport rectanble.
         * @param width The new width of the window.
         * @param height The new height of the window.
         */
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
            glu.gluPerspective(45.0f, h, 1.0, 20.0);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();
        }

        /** Invoked when a key has been pressed.
         * See the class description for {@link KeyEvent} for a definition of
         * a key pressed event.
         * @param e The KeyEvent.
         */
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                animator.stop();
                System.exit(0);
            }

        }

        /** Invoked when a key has been released.
         * See the class description for {@link KeyEvent} for a definition of
         * a key released event.
         * @param e The KeyEvent.
         */
        public void keyReleased(KeyEvent e) {}

        /** Invoked when a key has been typed.
         * See the class description for {@link KeyEvent} for a definition of
         * a key typed event.
         * @param e The KeyEvent.
         */
        public void keyTyped(KeyEvent e) {}
    }


    /** Program's main entry point
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        Frame frame = new Frame("Lesson 5: 3D Shapes");
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

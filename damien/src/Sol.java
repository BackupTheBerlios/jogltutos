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
            KeyListener, MouseListener, MouseMotionListener {

        private float rtri = 0.0f;
        private float angleZ = 0;
        GLDrawable gld;
        QCam cam = new QCam();
        public void display(GLDrawable gLDrawable) {
            gld = gLDrawable;
            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();
            GLUT glut = new GLUT();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            //gl.glTranslatef(0.f,0.f,-5.f);
            gl.glLoadIdentity();
            glu.gluPerspective(65.0f, 0, 0, -5);
            cam.setPerspective(gLDrawable);
/*
            gl.glRotatef(rtri, 0, 0, 1);
            gl.glTranslatef(0, angleZ, -20);
*/
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
            rtri += 0.01f;

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
            gLDrawable.addKeyListener(cam);
            gLDrawable.addMouseMotionListener(cam);
            gl.glEnable(gl.GL_NORMALIZE);
            gl.glEnable(gl.GL_LINE_SMOOTH);
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
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                angleZ += 0.1;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                angleZ -= 0.1;
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

        public void mouseClicked(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            System.out.println("Coucou");
        }
        int newX, newY, oldX, oldY,dx, dy;
        public void mouseDragged(MouseEvent e) {
                newX = e.getX();
                newY = e.getY();
                dx = newX - oldX;
                dy = newY - oldY;
 /*
                switch (typeDeplacement) {
                  // TRANSLATION
                  case 1:
                    double tx = (obj.tailleBBox * dx * 3.5) / (cam.resX / 2);
                    double ty = (obj.tailleBBox * dy * 3.5) / (cam.resY / 2);
                    ObjectUtil.translateObject(obj, tx, ty, 0);
                    drawObject(obj, cam);
                    break;
                    // ZOOM
                  case 2:
                    tx = (obj.tailleBBox * dy * 3.5) / (cam.resX / 2);
                    ObjectUtil.translateObject(obj, 0, 0, -tx);
                    drawObject(obj, cam);
                    break;
                    // ROTATION
                  case 0:*/
                    //ObjectUtil.bougeBoule(oldX, oldY, newX, newY, obj, cam);
                    //drawObject(obj, cam);
                    int resX = 800;
                    int resY = 600;
                    double rayon = Math.min(resX / 2., resY / 2.);
                    double vx, vy, vz;
   // X et Y dans le repère sphère
     vx = newX - resX / 2.;
     vy = newY - resY / 2.;

     if ( (vx*vx + vy*vy) < rayon*rayon )
       vz = -(double)Math.sqrt(rayon*rayon - vx*vx - vy*vy); // Dans la sphère
     else vz = 0; // Hors de la sphère
     gld.getGLU().gluLookAt(0,0,-50,vx,vy,vz,0,0,1);
                    oldX = newX;
                    oldY = newY;
      }

        public void mouseMoved(MouseEvent e) {

        }


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

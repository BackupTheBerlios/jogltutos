import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.util.*;
import net.java.games.jogl.*;

public class Flight {

    static Animator animator = null;
static int i;
    static class Renderer implements GLEventListener,
            KeyListener, MouseListener, MouseMotionListener {

        private float rtri = 0.0f;
        private float angleZ = 0;
        GLDrawable gld;
        public void display(GLDrawable gLDrawable) {
            gld = gLDrawable;
            final GL gl = gLDrawable.getGL();
            GLUT glut = new GLUT();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();

            //gl.glRotatef(rtri, 0, 0, 1);



            gl.glTranslatef(0, angleZ, -20);

            //gl.glMultMatrixd(mat);
            //initMat();

            gl.glRotated(alpha,nx,ny,nz);
            //gl.glRotated(salpha,snx,sny,snz);


            //System.out.println(alpha + " " + nx + " "+ ny+" " +nz);
            //gl.glRotated(xrot,1,0,0);
            //gl.glRotated(yrot,0,1,0);

            //System.out.println(angleRot);
            int L = 5; // Longueur de la surface
            int l = 5; // Largeur de la surface
            float t = 2f; // taille des triangles

            glut.glutWireCube(gl, 8);
            if (1 != 1) {
                return;
            }
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

        private void initMat(){
            mat[1] = mat[2] = mat[3] = mat[4] = mat[6] = mat[7] = 0;
            mat[8] = mat[9] = mat[11] = mat[12] = mat[13] = mat[14] = 0;
            mat[0] = mat[5] = mat[10] = mat[15] = 1;
        }

        public void displayChanged(GLDrawable gLDrawable, boolean modeChanged,
                                   boolean deviceChanged) {
        }

        public void init(GLDrawable gLDrawable) {
            final GL gl = gLDrawable.getGL();

            initMat();
            //gl.glShadeModel(gl.GL_FLAT); // Enable Smooth Shading
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
            gl.glClearDepth(1.0f); // Depth Buffer Setup
            gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // Really Nice Perspective Calculations
            gLDrawable.addKeyListener(this);
            gLDrawable.addMouseMotionListener(this);
            gLDrawable.addMouseListener(this);

            gl.glEnable(gl.GL_NORMALIZE);
            gl.glEnable(gl.GL_LINE_SMOOTH);
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
            glu.gluPerspective(90.0f, h, 1.0, 2000.0);
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
            oldX = e.getX();
            oldY = e.getY();
            //alpha = nx = ny = nz = 0;
        }
        double salpha, snx, sny, snz;
        public void mouseReleased(MouseEvent e) {

            salpha = alpha;
            snx = nx;
            sny = ny;
            snz = nz;
            //alpha = nx = ny = nz = 0;
        }

        double nx, ny, nz, angleRot, xrot, yrot, alpha;
        double newX, newY, oldX, oldY, dx, dy;
        double[] mat = new double[16];

        public void mouseDragged(MouseEvent e) {
            newX = e.getX();
            newY = e.getY();


            int resX = e.getComponent().getWidth();
            int resY = e.getComponent().getHeight();
            double rayon = Math.min(resX / 2., resY / 2.);



            double vx, vy, vz, vox, voy, voz;
            vx = newX - resX / 2.;
            vy = newY - resY / 2.;
            vox = oldX - resX / 2.;
            voy = oldY - resY / 2.;
            System.out.println("("+vox+","+voy+")"+ " " + "("+vx+","+vy+")");
            if ((vx * vx + vy * vy) < rayon * rayon)
                vz = -(double) Math.sqrt(rayon * rayon - vx * vx - vy * vy); // Dans la sphère
            else vz = 0; // Hors de la sphère

            if ((vox * vox + voy * voy) < rayon * rayon)
                voz = -(double) Math.sqrt(rayon * rayon - vox * vox - voy * voy); // Dans la sphère
             else voz = 0; // Hors de la sphère

             double normeO = Math.sqrt(vox * vox + voy * voy + voz * voz);
             double normeN = Math.sqrt(vx * vx + vy * vy + vz * vz);
             vox /= -normeO;
             voy /= normeO;
             voz /= normeO;
             vx /= -normeN;
             vy /= normeN;
             vz /= normeN;

             double ps = vox * vx + voy * vy + voz * vz;
             alpha = Math.acos(ps) * 180 / Math.PI;

             System.out.println("(" + vox + ","+voy+","+voz+")"+" ("+vx+","+vy+","+vz+")" + " alpha: "+alpha);


             nx = voy * vz - voz * vy;
            //if (dx > 0) nx = -nx;
            ny = voz * vx - vox * vz;
            //if (dy > 0) ny = -ny;
            nz = vox * vy - voy * vx;

            double norme = Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= norme;
            ny /= norme;
            nz /= norme;
           // nz = -nz;
            if (1==1) return;

            //double normeO = Math.sqrt(vox * vox + voy * voy + voz * voz);
            //double normeN = Math.sqrt(vx * vx + vy * vy + vz * vz);
            angleRot = Math.acos(ps / (normeO * normeN) * Math.PI / 180);
            angleRot = alpha;
            //System.out.println(ps + " " + normeO + " "+ normeN + " " + ps/(normeO*normeN));
            //prodScal = Matrix4.dotProdPoint3D(v1, v2);
            //angleRot = Math.acos(ps) * 180 / Math.PI;
            //angleRot += Math.acos(vox*vx+voy*vy+voz*vz)/Math.sqrt((vox*vox+voy*voy+voz*voz)*(vx*vx+vy*vy+vz*vz));

            //(XaXb+YaYb+ZaZb) / sqrt((Xa²+Ya²+Za²)(Xb²+Yb²+Zb²))
            //yz'-y'z , zx'-z'x , xy'-x'y
            double sin_a = Math.sin( angleRot / 2 );
            double cos_a = Math.cos( angleRot / 2 );

            nx = nx * sin_a;
            ny = -ny * sin_a;
            nz = -nz * sin_a;
            angleRot = cos_a;
            //System.out.println("Rx: " + nx + " Ry: " + ny + " Rz: " + nz +
            //                   " alpha: " + angleRot + " " + voz + " " + vz);
    //quaternion_normalise( q );

            double nw = angleRot;
            double xx = nx * nx;
            double xy = nx * ny;
            double xz = nx * nz;

            double xw = nx * nw;
            double yy = ny * ny;
            double yz = ny * nz;
            double yw = ny * nw;
            double zz = nz * nz;
            double zw = nz * nw;

            mat[0] = 1 - 2 * (yy + zz);
            mat[1] = 2 * (xy - zw);
            mat[2] = 2 * (xz + yw);

            mat[4] = 2 * (xy + zw);
            mat[5] = 1 - 2 * (xx + zz);
            mat[6] = 2 * (yz - xw);

            mat[8] = 2 * (xz - yw);
            mat[9] = 2 * (yz + xw);
            mat[10] = 1 - 2 * (xx + yy);

            mat[3] = mat[7] = mat[11] = mat[12] = mat[13] = mat[14] = 0;
            mat[15] = 1;
            //oldX = newX;
            //oldY = newY;
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

/*
 * This source uses the JoGL libraries found on the jogl.dev.java.net website.
 * This site (http://today.java.net/pub/a/today/2003/09/11/jogl2d.html) is the resource
 * I used to place the libraries into the proper directories...Setting up JoGL is pretty
 * simple follow these steps:
 *  - Download the libraries from https://jogl.dev.java.net/servlets/ProjectDocumentList
 *  - Extract the file jogl.jar to JRE_HOME/lib/ext
 *  - Extract the dll files to JRE_HOME/bin
 * The rest is the rest. If you do not understand java...Learn it somewhere else,
 * this assumes a level of understanding about the language. Good luck and enjoy.
 *  - @see author
 */

/*
 * Lesson10.java
 *
 * Created on December 20, 2003, 9:05 PM
 */

 //the only class in the package but it was easier to do this in porting the OpenGL tuts

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;
import java.net.*;
import net.java.games.jogl.*;
import net.java.games.jogl.util.GLUT;   //JoGL Lib Imports


public class QuakeLike extends Frame implements GLEventListener, KeyListener
{
    /** the number of textures, really the number of filters but it produces 3
     *  different looking textures, thus the name
     */
    private final int NUM_TEXTURES = 3;

    /** the number of keys that we want to pay attention to */
    private final int NUM_KEYS = 250;

    /** the float value of PI/180 */
    private final float PI_OVER_180 = (float)(Math.PI/180.0);

    /** fullscreen or not, true means yes */
    private boolean fullscreen = true;
    /** is blending on or off */
    private boolean blending;
    /** is the key 'B' pressed or not, referenced in display() for blending*/
    private boolean bp;
    /** is the key 'F' pressed or not, referenced in display() for filtering*/
    private boolean fp;

    /** the array of textures for our objects */
    private int[] textures = new int[NUM_TEXTURES];
    /** the array of keys to store whether certain actions should be taken based on
     * their values
     */
    private boolean[] keys = new boolean[NUM_KEYS];
    /** the value of filtering determines the filter value */
    private int filtering = 0;
    /** the x position */
    private float xpos;
    /** the rotation value on the y axis */
    private float yrot;
    /** the z position */
    private float zpos;
    private float heading;
    /** walkbias for head bobbing effect */
    private float walkbias = 0.0f;
    /** the angle used in calculating walkbias */
    private float walkbiasangle = 0.0f;
    /** the value used for looking up or down pgup or pgdown */
    private float lookupdown = 0.0f;

    /** a GLCanvas object */
    private GLCanvas glCanvas;
    /** an Animator object */
    private Animator animator;
    /** a sector which holds a series of triangles*/


    private QCam cam = new QCam();
    //holds a series of polygons, in this case triangles

    public QuakeLike(Dimension dim, boolean fscreen)
    {
        super("Creating a World...");
        fullscreen = fscreen;
        if (fullscreen == true)
            super.setSize(Toolkit.getDefaultToolkit().getScreenSize().getSize());
        else
            super.setSize(dim);
        filtering = 0;
        blending = false;
        // create a GLCapabilities object for the requirements for GL
        GLCapabilities glCapabilities = new GLCapabilities();
        //create a GLCamvas based on the requirements from above
        glCanvas = GLDrawableFactory.getFactory().createGLCanvas(glCapabilities);
        // add a GLEventListener, which will get called when the
        // canvas is resized or needs to be repainted
        glCanvas.addGLEventListener(this);
        //add the content page to the frame
        add(glCanvas, java.awt.BorderLayout.CENTER);
        animator = new Animator(glCanvas);
    }

    /** Called in the beginning of the application to take grab the focus on the
     * monitor of all other apps.
     * @return glCanvas
     */
    public GLCanvas getGLCanvas()
    {
        return glCanvas;
    }

    /** Called in the beginning of the application to grab the animator for the
     * canvas
     * @return animator
     */
    public Animator getAnimator()
    {
        return animator;
    }

    /*
     * METHODS DEFINED BY GLEventListener
     */

    /** Called by drawable to initiate drawing
     * @param gLDrawable The GLDrawable Object
     */
    public void display(GLDrawable gLDrawable)
    {
        GL gl = gLDrawable.getGL();
        GLU glu = gLDrawable.getGLU();
        GLUT glut = new GLUT();
        // Clear Color Buffer, Depth Buffer
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
        gl.glLoadIdentity();

        cam.setPerspective(gLDrawable);
        //gl.glTranslated(0,0,5);
        //glut.glutWireCube(gl,3);
        double c = 50;
        double h = -1;
        gl.glBegin(gl.GL_TRIANGLE_STRIP);
        gl.glColor3d(1,0,0);
        gl.glVertex3d(-c,h,-c);
        gl.glVertex3d(-c,h,c);
        gl.glVertex3d(0,h,-c);

        gl.glColor3d(1,0,1);
        gl.glVertex3d(0,h,c);

        gl.glColor3d(0,1,0);
        gl.glVertex3d(c,h,-c);

        gl.glColor3d(0,1,1);
        gl.glVertex3d(c,h,c);

        gl.glEnd();







    }

    /** Called by drawable to show that a mode or device has changed <br>
     * <B>!! CURRENTLY NON-Functional IN JoGL !!</B>
     * @param gLDrawable The GLDrawable object.
     * @param modeChanged Indicates if the video mode has changed.
     * @param deviceChanged Indicates if the video device has changed.
     */
    public void displayChanged(GLDrawable gLDrawable,
                                boolean modeChanged,
                                boolean deviceChanged)
    {
    }

    /**  Called by the drawable immediately after the OpenGL context is
     * initialized for the first time. Can be used to perform one-time OpenGL
     * initialization such as setup of lights and display lists.
     * @param gLDrawable The GLDrawable object.
     */
    public void init(GLDrawable gLDrawable)
    {
        GLU glu = gLDrawable.getGLU();
        GL gl = gLDrawable.getGL();
        gLDrawable.setGL( new DebugGL(gLDrawable.getGL()));

        gl.glShadeModel(GL.GL_FLAT);
        gl.glLightModeli(gl.GL_LIGHT_MODEL_LOCAL_VIEWER, gl.GL_TRUE);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);	// Really Nice Perspective Calculations

        cam.centerMouse(this);
        gLDrawable.addKeyListener(cam);            // Listening for key events
        gLDrawable.addMouseListener(cam);
        gLDrawable.addMouseMotionListener(cam);




    }

    /** This method loads textures into the texture array
     * @param gl A GL object to reference when setting values for it
     * @param glu A GLU object to reference when setting values for it
     * @param imgLoc The string location of the image/texture to load.
     */


    /** Reads the lines of the textfile sent as a parameter and loads them
     * into the sector
     * @param worldDataLoc The location of the textfile with the data
     */

    /** Called by the drawable during the first repaint after the component has
     * been resized. The client can update the viewport and view volume of the
     * window appropriately, for example by a call to
     * GL.glViewport(int, int, int, int); note that for convenience the component
     * has already called GL.glViewport(int, int, int, int)(x, y, width, height)
     * when this method is called, so the client may not have to do anything in
     * this method.
     * @param gLDrawable The GLDrawable object.
     * @param x The X Coordinate of the viewport rectangle.
     * @param y The Y coordinate of the viewport rectangle.
     * @param width The new width of the window.
     * @param height The new height of the window.
     */
    public void reshape(GLDrawable gLDrawable,
                        int x,
                        int y,
                        int width,
                        int height)
    {
        GLU glu = gLDrawable.getGLU();
        GL gl = gLDrawable.getGL();

        if (height <= 0) // avoid a divide by zero error!
            height = 1;
        float h = (float)width / (float)height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1, 1000);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    /** Forced by KeyListener; listens for keypresses and
     * sets a value in an array if they are not of
     * KeyEvent.VK_ESCAPE or KeyEvent.VK_F1
     * @param ke The KeyEvent passed from the KeyListener
     */
    public void keyPressed(KeyEvent ke)
    {
       switch(ke.getKeyCode())
       {
            case KeyEvent.VK_ESCAPE:
            {
                 System.out.println("User closed application.");
                 animator.stop();
                 System.exit(0);
                 break;
            }
            case KeyEvent.VK_F1:
            {
                setVisible(false);
                if (fullscreen)
                    setSize(800,600);
                else
                    setSize(Toolkit.getDefaultToolkit().getScreenSize().getSize());
                fullscreen = !fullscreen;
                //reshape();
                setVisible(true);
            }
            default :
               if(ke.getKeyCode()<250) // only interested in first 250 key codes, are there more?
                  keys[ke.getKeyCode()]=true;
               break;
         }
    }

    /** Unsets the value in the array for the key pressed.
     * @param ke The KeyEvent passed from the KeyListener
     */
    public void keyReleased(KeyEvent ke)
    {
        if (ke.getKeyCode() < 250) { keys[ke.getKeyCode()] = false; }
    }

    /** ...has no purpose in this class :)
     * @param ke The KeyEvent passed from the KeyListener
     */
    public void keyTyped(KeyEvent ke) {}


    public static void main(String[] args)
    {
        Dimension dim = new Dimension(800, 600);
        boolean fscreen = false;

        OptionAffichage options = new OptionAffichage();
        while(!(options.getOK()) && !(options.getCancel()))
        {
            try { Thread.sleep(5); } catch(InterruptedException ie) {}
        }
        if (options.getCancel())
        {
            System.out.println("User closed application.");
            System.exit(0);
        }
        fscreen = (options.getFullscreen()? true:false);
        dim = options.getPixels();
        //options.getBPP();
        options.setOff();
        options = null;

        QuakeLike lesson10 = new QuakeLike(dim, fscreen);
        lesson10.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        lesson10.setUndecorated(true);
        lesson10.show();
        lesson10.getAnimator().start();
        lesson10.getGLCanvas().requestFocus();
    }
}

/*
 * AtomicView.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package edu.rit.poe.atomix.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import edu.rit.poe.atomix.AtomicActivity;
import edu.rit.poe.atomix.R;
import edu.rit.poe.atomix.game.GameController;
import edu.rit.poe.atomix.game.GameException;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Connector;
import edu.rit.poe.atomix.levels.Square;
import edu.rit.poe.atomix.util.Point;

/**
 * The primary view of the DroidAtomix game.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class AtomicView extends View {
    
    public static final String LOG_TAG = "DROID_ATOMIX";
    
    public static final int BGCOLOR = Color.GRAY;
    
    public static final int FGCOLOR = Color.parseColor( "#F1E9D9" );
    
    public static final int HOVER_COLOR = Color.argb( 100, 255, 0, 0 );
    
    private static final int SQUARE_AREA = 11;
    
    private static final float TRACKBALL_MOVE_SUM = 0.50f;
    
    private static final int[] wallId;
    
    static {
        wallId = new int[ 15 ];
        
        wallId[ 0 ] = R.drawable.wall_single;
        wallId[ 1 ] = R.drawable.wall_horizontal_right_end;
        wallId[ 2 ] = R.drawable.wall_horizontal_left_end;
        wallId[ 3 ] = R.drawable.wall_horizontal;
        wallId[ 4 ] = R.drawable.wall_vertical_top_end;
        wallId[ 5 ] = R.drawable.wall_top_right;
        wallId[ 6 ] = R.drawable.wall_top_left;
        wallId[ 7 ] = R.drawable.wall_horizontal_bottomx;
        wallId[ 8 ] = R.drawable.wall_vertical_bottom_end;
        wallId[ 9 ] = R.drawable.wall_bottom_right;
        wallId[ 10 ] = R.drawable.wall_bottom_left;
        wallId[ 11 ] = R.drawable.wall_horizontal_topx;
        wallId[ 12 ] = R.drawable.wall_vertical;
        wallId[ 13 ] = R.drawable.wall_vertical_leftx;
        wallId[ 14 ] = R.drawable.wall_vertical_rightx;
    }
    
    /** The controller of the application. */
    private AtomicActivity atomix;
    
    private GameState gameState;
    
    private Map<Point, GameState.Direction> arrowSquares;
    
    private long trackballTime;
    
    private GameState.Direction trackballDir;
    
    private float trackballSum;
    
    private Rect gameArea;
    
    private int gameAreaOffsetX;
    
    private int gameAreaOffsetY;
    
    private int offsetX;
    
    private int offsetY;
    
    private int r;
    
    private int size;
    
    // if this ISN'T null, then the animation is running
    private SlideAnimation animation;
    
    private final Bitmap[] wallMap;
    
    private byte[][] walls;
    
    /**
     * An enumerated type to represent screen sizes of devices such as the HTC
     * Dream or Motorola Droid.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     */
    private static enum ScreenSize {
        
        /** The HTC Dream (T-Mobile G1) or a device with a 320x400 screen. */
        HVGA( 320, 480 ),
        
        /** The Motorola Droid or a device with a 480x854 screen. */
        WVGA( 480, 854 ),
        
        /** The Nexus One or a device with a 480x800 screen. */
        WVGA800( 480, 800 ),
        
        /** A device with a different screen size. */
        OTHER( -1, -1 );
        
        /** The width of the screen. */
        private int width;
        
        /** The height of the screen. */
        private int height;
        
        /**
         * Constructs a new <tt>ScreenSize</tt>.
         * 
         * @param   width   the device's screen width
         * @param   height  the device's screen height
         */
        ScreenSize( int width, int height ) {
            this.width = width;
            this.height = height;
        }
        
        /**
         * Returns <tt>true</tt> if this <tt>ScreenSize</tt> has an identical
         * size to the specified <tt>DisplayMetrics</tt>.
         * <p>
         * This method will text both portrait and landscape mode.
         * 
         * @param   dm  the <tt>DisplayMetrics</tt> to compare this screen to
         * 
         * @return      <tt>true</tt> if the screen size is equal, otherwise
         *              <tt>false</tt>
         */
        public boolean equals( DisplayMetrics dm ) {
            return ( ( ( dm.widthPixels == this.width ) &&
                    ( dm.heightPixels == this.height  ) ) ||
                    ( ( dm.heightPixels == this.width ) &&
                    ( dm.widthPixels == this.height  ) ) );
        }
        
        /**
         * Simple static methods to determine is the width is larger than the
         * height, indicative of a landscape display mode.
         * 
         * @param   dm  the display metrics to check for landscape mode
         * 
         * @return      <tt>true</tt> if the display metrics are in landscape
         *              mode, otherwise <tt>false</tt>
         */
        public static boolean isLandscape( DisplayMetrics dm ) {
            return ( dm.widthPixels > dm.heightPixels );
        }
        
    } // ScreenSize
    
    /**
     * Constructs a new <tt>AtomicView</tt>.
     * 
     * @param   atomix  the Atomix Activity that launched this view
     */
    public AtomicView( AtomicActivity atomix, GameState gameState ) {
        super( atomix );
        super.setScrollContainer( false );
        super.setClickable( true );
        
        this.atomix = atomix;
        this.setGameState( gameState );
        
        trackballTime = 0L;
        
        // load all wall bitmaps
        Resources res = atomix.getResources();
        wallMap = new Bitmap[ wallId.length ];
        for ( int i = 0; i < wallId.length; i++ ) {
            wallMap[ i ] = BitmapFactory.decodeResource( res, wallId[ i ] );
        }
    }
    
    /**
     * Sets the view's game state.
     * 
     * @param   gameState   the new backing game state
     */
    public void setGameState( GameState gameState ) {
        this.gameState = gameState;
        
        // squares that will be drawn as arrows
        arrowSquares = new HashMap<Point, GameState.Direction>();
        
        // stuff that got moved from onDraw() ==================================
        
        //
        // DISPLAY SETTINGS:
        //
        // HTC DREAM (320x480 phones):
        //     28x28 squares, with 1px on each side
        //     TOTAL game area:  320x320
        //
        // MOTOROLA DROID (854x480 phones):
        //     35x35 squares, with 1px on each side (and 2px left, 1px right)
        //     TOTAL game area: 480x480
        //
        DisplayMetrics dm = new DisplayMetrics();
        atomix.getWindowManager().getDefaultDisplay().getMetrics( dm );
        
        size = 0; // size of a single block (s x s)
        
        Log.d( "Display Metrics: ",
                "W: " + dm.widthPixels + " H: " + dm.heightPixels );
        
        // if G1
        if ( ScreenSize.HVGA.equals( dm ) ) {
            Log.d( "DROID_ATOMIX",
                    "This is not the Droid you are looking for." );
            
            size = 29;
            gameAreaOffsetX = 1;
            gameAreaOffsetY = 2;
            
            // else if Droid
        } else if ( ScreenSize.WVGA.equals( dm ) ) {
            Log.d( "DROID_ATOMIX", "This IS the Droid you are looking for." );
            
            size = 43;
            gameAreaOffsetX = 4;
            gameAreaOffsetY = 4;
            
        } else if ( ScreenSize.WVGA800.equals( dm ) ) {
            Log.d( LOG_TAG, "Voight-Kampff test: Nexus replicant found." );
            
            size = 43;
            gameAreaOffsetX = 4;
            gameAreaOffsetY = 4;
            
        } else {
            Log.d( "DROID_ATOMIX", "Resorting to HTC Dream display size." );
            
            size = 29;
            gameAreaOffsetX = 1;
            gameAreaOffsetY = 2;
        }
        Log.d( "SIZE", "Size: " + size );
        
        // calculate the atom radius
        r = ( int )Math.floor( ( double )size / 2.0d );
        
        
        // grab the board from the game state
        Square[][] board = gameState.getBoard();
        int boardWidth = board[ 0 ].length;
        int boardHeight = board.length;
        
        gameArea = new Rect( 0, 0, ( SQUARE_AREA * size ),
                ( SQUARE_AREA * size ) );
        
        
        // calculate centering offset
        
        offsetX = gameAreaOffsetX;
        offsetY = gameAreaOffsetY;
        offsetX += ( ( float )( ( SQUARE_AREA - boardWidth ) * size ) ) / 2.0f;
        offsetY += ( ( float )( ( SQUARE_AREA - boardHeight ) * size ) ) / 2.0f;
        
        
        // setup the map of wall types
        walls = new byte[ board.length ][ board[ 0 ].length ];
        for ( int i = 0; i < board[ 0 ].length; i++ ) {
            for ( int j = 0; j < board.length; j++ ) {
                if ( board [ j ][ i ] instanceof Square.Wall ) {
                    boolean up = ( ( j != 0 ) &&
                            ( board[ j - 1 ][ i ] instanceof Square.Wall ) );
                    
                    boolean down = ( ( j < ( board.length - 1 ) ) &&
                            ( board[ j + 1 ][ i ] instanceof Square.Wall ) );
                    
                    boolean right = ( ( i < ( board[ 0 ].length - 1 ) ) &&
                            ( board[ j ][ i + 1 ] instanceof Square.Wall ) );
                    
                    boolean left = ( ( i != 0 ) &&
                            ( board[ j ][ i - 1 ] instanceof Square.Wall ) );
                    
                    // set the bits for the wall id
                    byte val = 0;
                    val |= ( ( left  ? 1 : 0 ) << 0 );
                    val |= ( ( right ? 1 : 0 ) << 1 );
                    val |= ( ( down  ? 1 : 0 ) << 2 );
                    val |= ( ( up    ? 1 : 0 ) << 3 );
                    
                    walls[ j ][ i ] = val;
                }
            }
        }
    }
    
    /**
     * Draw the Atomix view.  This is the centerpiece of the View portion of the
     * application; everything drawn to the screen is done so inside this
     * method.
     * 
     * @param   canvas  the canvas to draw to
     */
    @Override
    public void onDraw( Canvas canvas ) {
        Paint p = new Paint();
        p.setColor( BGCOLOR );
        
        Rect rect = new Rect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        canvas.drawRect( rect, p );
        
        // grab the board from the game state
        Square[][] board = gameState.getBoard();
        
        // set the squares where arrows should be drawn (if the game isn't
        // finished)
        if ( ! gameState.isFinished() ) {
            this.setArrowSquares();
        } else {
            // no arrows!
            arrowSquares.clear();
        }
        
        // translate to create the 1px top/left gap
        canvas.save();
        canvas.translate( offsetX, offsetY );
        
        GameState.Direction dir = null;
        for ( int i = 0; i < board[ 0 ].length; i++ ) {
            for ( int j = 0; j < board.length; j++ ) {
                Square sqr = board[ j ][ i ];
                int left = i * size;
                int top = j * size;
                
                // translate to this square area
                canvas.save();
                canvas.translate( ( left + r ), ( top + r ) );
                
                Rect sq = new Rect( -r, -r, r, r );
                p.setColor( BGCOLOR );
                canvas.drawRect( sq, p );
                
                try {
                    if ( sqr instanceof Square.Wall ) {
                        //p.setColor( colorred );
                        //canvas.drawRect( sq, p );
                        
                        // draw a wall
                        Bitmap b = wallMap[ walls[ j ][ i ] ];
                        
                        canvas.drawBitmap( b, null, sq, null );
                        
                    } else if ( sqr instanceof Square.Empty ) {
                        // check whether this square should represent an arrow
                        if ( ( dir = arrowSquares.get( new Point( i, j ) ) ) !=
                                null ) {
                            p.setColor( FGCOLOR );
                            canvas.drawRect( sq, p );
                            
                            // don't draw arrows until animation has finished
                            if ( animation == null ) {
                                drawArrow( canvas, dir, r );
                            }
                        } else {
                            p.setColor( FGCOLOR );
                            canvas.drawRect( sq, p );
                        }
                    } else if ( sqr instanceof Atom ) {
                        Atom atom = ( Atom )sqr;
                        
                        // draw the square background
                        p.setColor( FGCOLOR );
                        canvas.drawRect( sq, p );
                        
                        // don't draw the selected atom during an animation
                        if ( ( animation == null ) ||
                                ( ! gameState.getSelected().equals( i, j ) ) ) {
                            // draw the atom to the board
                            drawAtom( canvas, atom, r, false );
                        }
                    } else {
                        p.setColor( BGCOLOR );
                        canvas.drawRect( sq, p );
                    }
                } catch ( Exception e ) {
                    p.setColor( BGCOLOR );
                }
                
                // does this square as the hover point?
                // also!  don't draw the hover point if the animation is running
                // also!  don't draw the hover point if the game is finished
                Point hoverPoint = gameState.getHoverPoint();
                if ( ( hoverPoint != null ) && hoverPoint.equals( i, j ) &&
                        ( animation == null ) &&
                        ( ! gameState.isFinished() ) ) {
                    p.setColor( HOVER_COLOR );
                    
                    canvas.drawRect( sq, p );
                }
                
                // draw the rounded corners
                p.setColor( BGCOLOR );
                
                canvas.save();
                
                // ul
                Rect corner = new Rect( -r, -r, ( -r + 1 ), ( -r + 1 ) );
                canvas.drawRect( corner, p );
                // ur
                canvas.rotate( 90.0f );
                canvas.drawRect( corner, p );
                // ll
                canvas.rotate( 90.0f );
                canvas.drawRect( corner, p );
                // lr
                canvas.rotate( 90.0f );
                canvas.drawRect( corner, p );
                
                canvas.restore();
                
                // pop the matrix stack :)
                canvas.restore();
            }
        }
        
        // if we're animating, then draw the sliding atom
        if ( animation != null ) {
            Atom atom = animation.atom;
            canvas.save();
            
            // offset to the right location
            int x = ( int )( animation.currentX * size );
            int y = ( int )( animation.currentY * size );
            canvas.translate( x, y );
            
            // draw the atom at the offset located
            canvas.save();
            canvas.translate( r, r );
            drawAtom( canvas, atom, r, false );
            canvas.restore();
            
            canvas.restore();
        }
        
        canvas.restore();
        
        // === draw the solution panel ===
        
        // where are we translating to?
        int t = ( SQUARE_AREA * size ) + gameAreaOffsetY;
        int solutionOffsetX = 0;
        int solutionOffsetY = t;
        int solutionWidth = super.getWidth() - 1;
        int solutionHeight = super.getHeight() - t - 1;
        // if we're landscape, the goal is off to the side
        DisplayMetrics dm = new DisplayMetrics();
        atomix.getWindowManager().getDefaultDisplay().getMetrics( dm );
        if ( ScreenSize.isLandscape( dm ) ) {
            solutionOffsetX = t;
            solutionOffsetY = 2;
            solutionWidth = super.getWidth() - t - 1;
            solutionHeight = super.getHeight() - 3;
        }
        canvas.translate( solutionOffsetX, solutionOffsetY );
        
        // set black background for solution area
        p.setColor( Color.BLACK );
        canvas.drawRect( 1, 0, solutionWidth, solutionHeight, p );
        
        // if we can support normal-sized squares, do so, otherwise scale
        Square[][] goal = gameState.getGoal();
        
        // draw the actual goal
        drawSolution( canvas, goal, size, solutionWidth, solutionHeight );
        
        // draw the strings
        drawSolutionInfo( canvas, gameState.getLevel(),
                gameState.getMoleculeName(), gameState.getFormula(),
                solutionWidth, solutionHeight );
    }
    
    /**
     * Draws the specified atom onto the canvas with its respective connectors.
     * <p>
     * The canvas should be translated to the center-point of the atom.
     * 
     * @param   canvas  the canvas to be drawn to
     * @param   atom    the atom to be drawn
     * @param   r       the radius of the square to draw the atom inside
     * @param   goal    whether this is a goal drawing
     */
    private static void drawAtom( Canvas canvas, Atom atom, int r,
            boolean goal ) {
        Paint p = new Paint();
        
        // draw the connectors first
        for ( Connector c : atom.getConnectors() ) {
            canvas.save();
            
            boolean cardinal = true;
            switch ( c.getDirection() ) {
                case DOWN: {
                    canvas.rotate( 180.0f );
                } break;
                case RIGHT: {
                    canvas.rotate( 90.0f );
                } break;
                case LEFT: {
                    canvas.rotate( -90.0f );
                } break;
                case UPPER_RIGHT: {
                    cardinal = false;
                } break;
                case UPPER_LEFT: {
                    canvas.rotate( -90.0f );
                    cardinal = false;
                } break;
                case LOWER_RIGHT: {
                    canvas.rotate( 90.0f );
                    cardinal = false;
                } break;
                case LOWER_LEFT: {
                    canvas.rotate( 180.0f );
                    cardinal = false;
                } break;
                
            }
            
            int len = r;
            int connColor = Color.BLACK;
            if ( goal ) {
                connColor = Color.WHITE;
                len = ( r + 1 );
            }
            p.setColor( connColor );
            
            // current only supports lines for G1-sized devices
            // @todo Droid connector support?
            if ( c.getBond() == Connector.Bond.SINGLE ) {
                if ( cardinal ) {
                    canvas.drawLine( 1, 0, 1, -len, p );
                    canvas.drawLine( 0, 0, 0, -len, p );
                    canvas.drawLine( -1, 0, -1, -len, p );
                } else {
                    canvas.drawLine( 1, 0, len, -( len - 1 ), p );
                    canvas.drawLine( 0, 0, len, -len, p );
                    canvas.drawLine( -1, 0, ( len - 1 ), -len, p );
                }
            } else if ( c.getBond() == Connector.Bond.DOUBLE ) {
                if ( cardinal ) {
                    canvas.drawLine( 3, 0, 3, -len, p );
                    canvas.drawLine( 2, 0, 2, -len, p );
                    
                    canvas.drawLine( -2, 0, -2, -len, p );
                    canvas.drawLine( -3, 0, -3, -len, p );
                } else {
                    // no such thing as a double-bond diagonal connector
                    canvas.drawLine( 1, 0, len, -( len - 1 ), p );
                    canvas.drawLine( 0, 0, len, -len, p );
                    canvas.drawLine( -1, 0, ( len - 1 ), -len, p );
                }
            }
            
            canvas.restore();
        }
        
        // draw the atom circle
        p.setColor( Color.parseColor( "#" + atom.getColor() ) );
        int radius = r - 4;
        p.setAntiAlias( true );
        canvas.drawCircle( 0, 0, radius, p );
        p.setAntiAlias( false );
        
        // draw the element letter
        p.setTypeface( Typeface.DEFAULT_BOLD );
        // @todo figure out text size for the Droid
        Rect bounds = new Rect();
        p.getTextBounds( new char[]{ atom.getElement() }, 0, 1, bounds );
        int tx = 0 - ( ( bounds.width() / 2 ) + 1 );
        int ty = 0 + ( bounds.height() / 2 );
        p.setColor( Color.WHITE );
        canvas.drawText( Character.toString( atom.getElement() ), tx, ty, p );
    }
    
    /**
     * Draws the specified solution configuration to the specified canvas.
     * <p>
     * The canvas should be translated to the top left corner of the solution
     * panel.
     * 
     * @param   canvas  the canvas to be drawn to
     * @param   goal    the goal configuration
     * @param   size    the size of each square on the board (default size for
     *                  this device)
     * @param   w       the width of the solution panel
     * @param   h       the height of the solution panel
     */
    public static void drawSolution( Canvas canvas, Square[][] goal,
            int size, int w, int h ) {
        int goalSize = size;
        int xNeeded = goal[ 0 ].length * size;
        int yNeeded = goal.length * size;
        int goalOffsetX = 0;
        int goalOffsetY = 0;
        
        // scale the goal size, if necessary
        if ( xNeeded > w ) {
            goalSize /= ( xNeeded / w );
        } else {
            goalOffsetX = ( w - xNeeded ) / 2;
        }
        if ( yNeeded > h ) {
            goalSize /= ( yNeeded / h );
        } else {
            goalOffsetY = ( h - yNeeded ) / 2;
        }
        
        // draw the goal
        canvas.save();
        canvas.translate( goalOffsetX, goalOffsetY );
        
        // move into first position
        canvas.translate( ( goalSize / 2 ), ( goalSize / 2 ) );
        
        for ( int i = 0; i < goal.length; i++ ) {
            canvas.save();
            for ( int j = 0; j < goal[ 0 ].length; j++ ) {
                if ( goal[ i ][ j ] instanceof Atom ) {
                    Atom atom = ( Atom )goal[ i ][ j ];
                    
                    // draw the atom to the board
                    drawAtom( canvas, atom, ( goalSize / 2 ), true );
                }
                
                // move right one square
                canvas.translate( goalSize, 0 );
            }
            canvas.restore();
            
            // move down one square
            canvas.translate( 0, goalSize );
        }
        
        canvas.restore();
    }
    
    /**
     * Draws the solution information to the solution panel.
     * <p>
     * The canvas should be translated to the top left corner of the solution
     * panel.
     * 
     * @param   canvas      the canvas to be drawn to
     * @param   level       the level number
     * @param   name        the name of the goal molecule
     * @param   formula     the goal molecule chemical formula
     * @param   w           the width of the solution panel
     * @param   h           the height of the solution panel
     */
    private static void drawSolutionInfo( Canvas canvas, int level, String name,
            String formula, int w, int h ) {
        Log.d( "Goal area", "w: " + w + " h: " + h );
        // draw the level and the molecule
        Paint p = new Paint();
        p.setTypeface( Typeface.DEFAULT_BOLD );
        p.setColor( Color.WHITE );
        
        Rect bounds = new Rect();
        String str = "Level " + level + " - " + name;
        p.getTextBounds( str, 0, 1, bounds );
        int tx = 5;
        int ty = bounds.height() + 5;
        canvas.drawText( str, tx, ty, p );
        
        ty += ( h - 25 );
        int mx = w - 7;
        for ( int i = ( formula.length() - 1 ); i >= 0; i-- ) {
            char c = formula.charAt( i );
            
            p.getTextBounds( new char[]{ c }, 0, 1, bounds );
            mx -= bounds.width();
            
            int y = ty;
            if ( ( i != 0 ) && ( formula.charAt( i - 1 ) == '_' ) ) {
                y += 5;
                i--; // increment over the _ element
            }
            canvas.drawText( Character.toString( c ), mx, y, p );
            mx -= 3;
        }
    }
    
    /**
     * Draws an arrow on the given canvas.
     * 
     * @param   canvas  the canvas to draw to (also translated to the proper
     *                  drawing location)
     * @param   dir     the direction of the arrow
     * @param   r       the radius of the square
     */
    private static void drawArrow( Canvas canvas, GameState.Direction dir,
            int r ) {
        Path path = new Path();
        path.moveTo( ( -r + 3 ), ( r - 3 ) );
        path.lineTo( ( r - 3 ), ( r - 3 ) );
        path.lineTo( 0, ( -r + 5 ) );
        
        canvas.save();
        
        switch ( dir ) {
            case DOWN: {
                canvas.rotate( 180.0f );
            } break;
            case RIGHT: {
                canvas.rotate( 90.0f );
            } break;
            case LEFT: {
                canvas.rotate( -90.0f );
            } break;
        }
        
        Paint paint = new Paint();
        paint.setColor( Color.GREEN );
        canvas.drawPath( path, paint );
        
        canvas.restore();
    }
    
    /**
     * Called on each touch event, and used to mark a hover point, select an
     * atom, or click on a direction that an atom is to be moved.
     * 
     * @param   event   the <tt>MotionEvent</tt> for this touch
     * 
     * @return          <tt>true</tt>, since the event will be handled
     */
    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        // ignore clicking if the game is finished
        if ( ! gameState.isFinished() ) {
            Square[][] board = gameState.getBoard();
            
            // what portion of the screen was clicked?
            int x = ( int )event.getX();
            int y = ( int )event.getY();
            
            // don't trigger a click outside the game area
            if ( gameArea.contains( x, y ) ) {
                int i = ( int )( ( x - offsetX ) / size );
                int j = ( int )( ( y - offsetY ) / size );
                
                // don't hover or click blank boxes
                try {
                    if ( board[ j ][ i ] != null ) {
                        // try to set as hoverpoint first and foremost
                        setHoverpoint( i, j, true );

                        if ( event.getAction() == MotionEvent.ACTION_UP ) {
                            Log.d( "TOUCH EVENT",
                                    "Selected at " + i + ", " + j );

                            // select the currently hovered square
                            touch( i, j );
                        }
                    }
                } catch ( ArrayIndexOutOfBoundsException e ) {
                    // ignore - this can get thrown sometimes, in special places
                }
            }
        }
        return true;
    }
    
    /**
     * Sets the current hover point and redraws the view if specified.
     * 
     * @param   i       the X coordinate
     * @param   j       the Y coordinate
     * @param   redraw  whether to redraw the view
     */
    private void setHoverpoint( int i, int j, boolean redraw ) {
        // is this a NEW hover point?
        Point hoverPoint = gameState.getHoverPoint();
        if ( ( hoverPoint == null ) ||
                ( ! hoverPoint.equals( i, j ) ) ) {
            gameState.setHoverPoint( new Point( i, j ) );

            // force a redraw
            if ( redraw ) {
                super.postInvalidate();
            }
        }
    }
    
    /**
     * Called when the trackball is moved.  This method controls the hover
     * location when the user is using the trackball to control the game.
     * 
     * @param   event   the trackball event
     * 
     * @return          <tt>true</tt>, since the event was handled
     */
    @Override
    public boolean onTrackballEvent( MotionEvent event ) {
        // switch on the type of action
        if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
            Log.d( "TRACKBALL", "PRESS EVENT" );
            
            // select the currently hovered square
            Point hoverPoint = gameState.getHoverPoint();
            touch( hoverPoint.x, hoverPoint.y );
            
        } else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
            float y = Math.abs( event.getY() );
            float x = Math.abs( event.getX() );
            
            GameState.Direction dir = null;
            float sum = 0.0f;
            if ( y > x ) {
                sum = event.getY();
                if ( event.getY() < 0 ) {
                    Log.d( "TRACKBALL", "Y up: " + event.getY() );
                    dir = GameState.Direction.UP;
                } else if ( event.getY() > 0 ) {
                    Log.d( "TRACKBALL", "Y down " + event.getY() );
                    dir = GameState.Direction.DOWN;
                }
            } else {
                sum = event.getX();
                if ( event.getX() < 0 ) {
                    Log.d( "TRACKBALL", "X left " + event.getX() );
                    dir = GameState.Direction.LEFT;
                } else if ( event.getX() > 0 ) {
                    Log.d( "TRACKBALL", "X right " + event.getX() );
                    dir = GameState.Direction.RIGHT;
                }
            }
            
            long curr = System.currentTimeMillis();
            if ( ( trackballDir != dir ) ||
                    ( ( curr - trackballTime ) > 500 ) ) {
                trackballDir = dir;
                trackballSum = 0.0f;
            }
            trackballSum += sum;
            
            if ( Math.abs( trackballSum ) > TRACKBALL_MOVE_SUM ) {
                Log.d( "TRACKBALL", "TRIGGERED!" );
                Point hoverPoint = gameState.getHoverPoint();

                // set the track point
                switch ( trackballDir ) {
                    case UP: {
                        if ( isHoverable( hoverPoint.x,
                                ( hoverPoint.y - 1 ) ) ) {
                            hoverPoint.offset( 0, -1 );
                        }
                    } break;
                    case DOWN: {
                        if ( isHoverable( hoverPoint.x,
                                ( hoverPoint.y + 1 ) ) ) {
                            hoverPoint.offset( 0, 1 );
                        }
                    } break;
                    case RIGHT: {
                        if ( isHoverable( ( hoverPoint.x + 1 ),
                                hoverPoint.y ) ) {
                            hoverPoint.offset( 1, 0 );
                        }
                    } break;
                    case LEFT: {
                        if ( isHoverable( ( hoverPoint.x - 1 ),
                                hoverPoint.y ) ) {
                            hoverPoint.offset( -1, 0 );
                        }
                    } break;
                }
                
                trackballSum = 0.0f;
            }
            
            super.postInvalidate();
            trackballTime = curr;
        }
        return true;
    }
    
    /**
     * Determines whether the specified location is hoverable.
     * 
     * @param   x   the X coordinate
     * @param   y   the Y coordinate
     * 
     * @return      <tt>true</tt> is hovering on this point is allowed,
     *              otherwise <tt>false</tt>
     */
    private boolean isHoverable( int x, int y ) {
        boolean retVal = true;
        
        Square[][] board = gameState.getBoard();
        if ( ( x >= board[ 0 ].length ) || ( x < 0 ) ||
                ( y >= board.length ) || ( y < 0 ) ) {
            retVal = false;
        } else if ( board[ y ][ x ] == null ) {
            retVal = false;
        }
        
        return retVal;
    }
    
    /**
     * Touches the board at the specified location.  This method will trigger
     * selecting of atoms and clicking of arrows.
     * 
     * @param   i   the X coordinate
     * @param   j   the Y coordinate
     */
    private void touch( int i, int j ) {
        Square[][] board = gameState.getBoard();
        Point selected = gameState.getSelected();
        boolean ItsGoingToBeOkay = true;
        GameState.Direction d = null;
        
        try {
            if ( board[ j ][ i ] instanceof Atom ) {
                gameState.setSelected( new Point( i, j ) );
            } else if ( ( d = arrowSquares.get( new Point( i, j ) ) )
                    != null ) {
                // we clicked on an arrow -> move the selected atom
                Log.d( "Atomix:TouchEvent", "Touched an arrow: " + d );
                
                try {
                    int oldX = selected.x;
                    int oldY = selected.y;
                    Atom atom = ( Atom )board[ oldY ][ oldX ];
                    
                    boolean win = GameController.moveSelected( gameState, d );
                    
                    if ( win ) {
                        Log.d( "ATOMIC_VIEW", "this is a win..." );
                        if ( gameState.isFinished() ) {
                            Log.d( "ATOMIC_VIEW", "GameState FINISHED" );
                        }
                    }
                    
                    int newX = selected.x;
                    int newY = selected.y;
                    
                    animation = new SlideAnimation( atom, oldX, oldY, newX,
                            newY, win );
                    animation.start();
                    
                } catch ( GameException e ) {
                    // no currently selected atom.  this shouldn't happen. evar.
                    assert false;
                }
                
                // move the hover point to the new location of the Atom
                setHoverpoint( selected.x, selected.y, false );
            }
            
            // if only forgetting were this easy for me.
            assert ItsGoingToBeOkay;
            
            // force a redraw
            super.postInvalidate();
            
        } catch ( ArrayIndexOutOfBoundsException e ) {
            // touch out of bounds
        }
    }
    
    /**
     * Sets the locations to draw arrows on the board.
     */
    private void setArrowSquares() {
        
        // identify and store all Direction flags to be drawn
        EnumSet<GameState.Direction> directions =
                GameController.getPossibleDirections( gameState );
        
        arrowSquares.clear();
        for ( GameState.Direction dir : directions ) {
            int x = gameState.getSelected().x;
            int y = gameState.getSelected().y;
            switch ( dir ) {
                case UP: {
                    y--;
                } break;
                case DOWN: {
                    y++;
                } break;
                case RIGHT:  {
                    x++;
                } break;
                case LEFT: {
                    x--;
                }  break;
            }
            arrowSquares.put( new Point( x, y ), dir );
        }
    }
    
    /**
     * A thread to animate the movement of an atom on the board.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     */
    private class SlideAnimation extends Thread {
        
        /** The number of animation frames per square moved. */
        public static final int FRAMES_PER_SQR = 2;
        
        /** The atom being slid. */
        private final Atom atom;
        
        /** The number of total frames of this animation. */
        private final int frames;
        
        /** The current X-coordinate position in units of board squares. */
        private float currentX;
        
        /** The current Y-coordinate position in units of board squares. */
        private float currentY;
        
        /** The number of squares to offset every frame on the X axis. */
        private final float offsetX;
        
        /** The number of squares to offset every frame on the Y axis. */
        private final float offsetY;
        
        private char orientation;
        
        /** Whether this move will cause a win at the end of the animation. */
        private boolean win;
        
        /**
         * Constructs a new <tt>SlideAnimation</tt> with the given variables.
         * 
         * @param   atom    the atom being slid
         * @param   startX  the initial X-coordinate of the selected atom
         * @param   startY  the initial Y-coordinate of the selected atom
         * @param   endX    the ending X-coordinate of the selected atom
         * @param   endY    the ending Y-coordinate of the selected atom
         * @param   win     whether this move causes a win
         */
        private SlideAnimation( Atom atom, int startX, int startY, int endX,
                int endY, boolean win ) {
            this.atom = atom;
            this.win = win;
            currentX = startX;
            currentY = startY;
            
            int dX = ( endX - startX );
            int dY = ( endY - startY );
            
            orientation = ( dX == 0 ) ? 'y' : 'x';
            
            // one of these is 0 (has to be), so take the easy answer!
            frames = Math.abs( ( dX + dY ) * FRAMES_PER_SQR );
            
            // find the amount by which to offset each frame
            offsetX = ( float )dX / ( float )frames;
            offsetY = ( float )dY / ( float )frames;
        }
        
        /**
         * Runs the animation to slide the selected atom from its initial
         * position to the ending position.
         */
        @Override
        public void run() {
            // loop over all frames
            for ( int frame = 0; frame < frames; frame++ ) {
                currentX += offsetX;
                currentY += offsetY;
                
                // draw the position of the atom
                atomix.redrawView( null );
                
                // sleep until the next frame
                try {
                    Thread.sleep( 50 );
                } catch ( InterruptedException e ) {
                    Log.e( "SlideAnimation", "Animation thread interrupted." );
                }
            }
            
            // at the end of the run, let this object commit suicide
            animation = null;
            
            // last redraw in position
            atomix.redrawView( null );
            
            // animation is over -- handle winning!
            if ( win ) {
                Log.d( "Atomix:GameState", "You win!" );
                // we won the level!
                atomix.winLevel();
            }
        }
        
    } // SlideAnimation
    
} // AtomicView

/*
 * AtomicView.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without major
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, and/or distribute copies of, but
 * under no circumstances sublicense and/or sell, the Software,
 * and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package edu.rit.poe.atomix.view;

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
import android.widget.Toast;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import edu.rit.poe.atomix.AtomicActivity;
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
    
    /** The controller of the application. */
    private AtomicActivity atomix;
    
    Map<Point, GameState.Direction> arrowSquares;
    
    private long trackballTime;
    
    private GameState.Direction trackballDir;
    
    private float trackballSum;
    
    private Rect gameArea;
    
    private int offsetX;
    
    private int offsetY;
    
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
        
        /** The Motorola Droid or a device with a 400x800 screen. */
        WVGA( 480, 854 ),
        
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
         * 
         * @param   dm  the <tt>DisplayMetrics</tt> to compare this screen to
         * 
         * @return      <tt>true</tt> if the screen size is equal, otherwise
         *              <tt>false</tt>
         */
        public boolean equals( DisplayMetrics dm ) {
            return ( ( dm.widthPixels == this.width ) &&
                    ( dm.heightPixels == this.height  ) );
        }
        
    } // ScreenSize
    
    /**
     * Constructs a new <tt>AtomicView</tt>.
     * 
     * @param   atomix  the Atomix Activity that launched this view
     */
    public AtomicView( AtomicActivity atomix ) {
        super( atomix );
        super.setScrollContainer( false );
        super.setClickable( true );
        
        this.atomix = atomix;
        
        // squares that will be drawn as arrows
        arrowSquares = new HashMap<Point, GameState.Direction>();
        // if this is being re-instated, set the arrow squares
        this.setArrowSquares();
        
        trackballTime = 0L;
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
        GameState gameState = GameState.getCurrent();
        
        int bgcolor = Color.GRAY;
        int fgcolor = Color.parseColor( "#F1E9D9" );
        int colorred = Color.parseColor( "#714444" );
        
        Paint p = new Paint();
        p.setColor( bgcolor );
        
        Rect rect = new Rect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        canvas.drawRect( rect, p );
        
        //
        // DISPLAY SETTINGS:
        //
        // HTC DREAM (320x480 phones):
        //     28x28 squares, with 1px on each side
        //     TOTAL game area:  320x320
        //
        // MOTOROLA DROID (400x800 phones):
        //     35x35 squares, with 1px on each side (and 2px left, 1px right)
        //     TOTAL game area: 400x400
        //
        DisplayMetrics dm = new DisplayMetrics();
        atomix.getWindowManager().getDefaultDisplay().getMetrics( dm );
        
        int SQUARE_AREA = 11; // how many squares in the S x S board
        int size = 0; // size of a single block (s x s)
        
        Log.d( "Display Metrics: ",
                "W: " + dm.widthPixels + " H: " + dm.heightPixels );
        
        // if G1
        if ( ScreenSize.HVGA.equals( dm ) ) {
            Log.d( "DROID_ATOMIX",
                    "This is not the Droid you are looking for." );
            
            size = 29;
            offsetX = 1;
            offsetY = 2;
            
            // else if Droid
        } else if ( ScreenSize.WVGA.equals( dm ) ) {
            Log.d( "DROID_ATOMIX", "This IS the Droid you are looking for." );
            
            size = 43;
            offsetX = 4;
            offsetY = 4;
            
        } else {
            Log.d( "DROID_ATOMIX", "Resorting to HTC Dream display size." );
            
            size = 28;
            offsetX = 1;
            offsetY = 2;
        }
        
        // grab the board from the game state
        Square[][] board = gameState.getBoard();
        int boardWidth = board[ 0 ].length;
        int boardHeight = board.length;
        gameArea = new Rect( 0, 0, ( SQUARE_AREA * size ),
                ( SQUARE_AREA * size ) );
        
        Log.d( "SIZE", "Size: " + size );
        
        // calculate centering offset
        
        offsetX += ( ( float )( ( SQUARE_AREA - boardWidth ) * size ) ) / 2.0f;
        offsetY += ( ( float )( ( SQUARE_AREA - boardHeight ) * size ) ) / 2.0f;
        //@todo account for offset when clicking
        
        // translate to create the 1px top/left gap
        canvas.save();
        canvas.translate( offsetX, offsetY );
        
        GameState.Direction dir = null;
        for ( int i = 0; i < board[ 0 ].length; i++ ) {
            for ( int j = 0; j < board.length; j++ ) {
                int left = i * size;
                int top = j * size;
                int r = ( int )Math.floor( ( double )size / 2.0d );
                
                // translate to this square area
                canvas.save();
                canvas.translate( ( left + r ), ( top + r ) );
                
                Rect sq = new Rect( -r, -r, r, r );
                p.setColor( bgcolor );
                canvas.drawRect( sq, p );
                
                try {
                    if ( board[ j ][ i ] instanceof Square.Wall ) {
                        p.setColor( colorred );
                        canvas.drawRect( sq, p );
                    } else if ( board[ j ][ i ] instanceof Square.Empty ) {
                        // check whether this square should represent an arrow
                        if ( ( dir = arrowSquares.get( new Point( i, j ) ) ) !=
                                null ) {
                            p.setColor( fgcolor );
                            canvas.drawRect( sq, p );
                            
                            drawArrow( canvas, dir, r );
                        } else {
                            p.setColor( fgcolor );
                            canvas.drawRect( sq, p );
                        }
                    } else if ( board[ j ][ i ] instanceof Atom ) {
                        Atom atom = ( Atom )board[ j ][ i ];
                        
                        // draw the square background
                        p.setColor( fgcolor );
                        canvas.drawRect( sq, p );
                        
                        // draw the atom to the board
                        drawAtom( canvas, atom, r, false );
                    } else {
                        p.setColor( bgcolor );
                        canvas.drawRect( sq, p );
                    }
                } catch ( Exception e ) {
                    p.setColor( bgcolor );
                }
                
                // does this square as the hover point?
                
                Point hoverPoint = gameState.getHoverPoint();
                if ( ( hoverPoint != null ) && hoverPoint.equals( i, j ) ) {
                    int hoverColor = Color.argb( 100, 255, 0, 0 );
                    p.setColor( hoverColor );
                    
                    canvas.drawRect( sq, p );
                }
                
                // draw the rounded corners
                p.setColor( bgcolor );
                
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
        
        int t = SQUARE_AREA * size;
        int solutionPanelWidth = super.getWidth() - 2;
        int solutionPanelHeight = super.getHeight() - t - 2;
        canvas.restore();
        canvas.translate( 0, t );
        
        // set black background for solution area
        p.setColor( Color.BLACK );
        canvas.drawRect( 1, 0, solutionPanelWidth, solutionPanelHeight, p );
        
        // === draw the solution ===
        
        // if we can support normal-sized squares, do so, otherwise scale
        Square[][] goal = gameState.getGoal();
        int goalSize = size;
        int xNeeded = goal[ 0 ].length * size;
        int yNeeded = goal.length * size;
        int goalOffsetX = 0;
        int goalOffsetY = 0;
        
        // scale the goal size, if necessary
        if ( xNeeded > solutionPanelWidth ) {
            goalSize /= ( xNeeded / solutionPanelWidth );
        } else {
            goalOffsetX = ( solutionPanelWidth - xNeeded ) / 2;
        }
        if ( yNeeded > solutionPanelHeight ) {
            goalSize /= ( yNeeded / solutionPanelHeight );
        } else {
            goalOffsetY = ( solutionPanelHeight - yNeeded ) / 2;
        }
        
        // draw the level and the molecule
        p.setTypeface( Typeface.DEFAULT_BOLD );
        Rect bounds = new Rect();
        p.setColor( Color.WHITE );
        String str = "Level " + gameState.getLevel() + " - "
                + gameState.getMoleculeName();
        p.getTextBounds( str, 0, 1, bounds );
        int tx = 5;
        int ty = bounds.height() + 5;
        canvas.drawText( str, tx, ty, p );
        
        String formula = gameState.getFormula();
        int mx = solutionPanelWidth - 5;
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
        
        // draw the goal
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
                } else {
                    // @todo anything here?
                }
                
                // move right one square
                canvas.translate( goalSize, 0 );
            }
            canvas.restore();
            
            // move down one square
            canvas.translate( 0, goalSize );
        }
    }
    
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
        Rect bounds = new Rect();
        p.getTextBounds( new char[]{ atom.getElement() }, 0, 1, bounds );
        int tx = 0 - ( ( bounds.width() / 2 ) + 1 );
        int ty = 0 + ( bounds.height() / 2 );
        p.setColor( Color.WHITE );
        canvas.drawText( Character.toString( atom.getElement() ), tx, ty, p );
        
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
        int action = event.getAction();
        
        // what portion of the screen was clicked?
        int x = ( int )event.getX();
        int y = ( int )event.getY();
        
        if ( gameArea.contains( x, y ) ) {
            int i = ( int )( ( x - offsetX ) / 29.0f );
            int j = ( int )( ( y - offsetY ) / 29.0f );
            //@todo ensure this works for maps larger, as well as smaller
            
            // are we setting the hover pointer, or selecting a square
            if ( ( action == MotionEvent.ACTION_DOWN ) ||
                    ( action == MotionEvent.ACTION_MOVE ) ) {
                // is this a new hover point?
                Point hoverPoint = GameState.getCurrent().getHoverPoint();
                if ( ( hoverPoint == null ) ||
                        ( ! hoverPoint.equals( i, j ) ) ) {
                    GameState.getCurrent().setHoverPoint( new Point( i, j ) );
                    
                    // force a redraw
                    super.postInvalidate();
                }
            } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
                Log.d( "TOUCH EVENT", "Selected at " + i + ", " + j );
                
                // select the currently hovered square
                touch( i, j );
            }
        }
        return true;
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
            Point hoverPoint = GameState.getCurrent().getHoverPoint();
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
            
            if ( Math.abs( trackballSum ) > .50 ) {
                Log.d( "TRACKBALL", "TRIGGERED!" );
                Point hoverPoint = GameState.getCurrent().getHoverPoint();

                // set the track point
                switch ( trackballDir ) {
                    case UP: {
                        if ( canMove( hoverPoint, 0, -1 ) ) {
                            hoverPoint.offset( 0, -1 );
                        }
                    } break;
                    case DOWN: {
                        if ( canMove( hoverPoint, 0, 1 ) ) {
                            hoverPoint.offset( 0, 1 );
                        }
                    } break;
                    case RIGHT: {
                        if ( canMove( hoverPoint, 1, 0 ) ) {
                            hoverPoint.offset( 1, 0 );
                        }
                    } break;
                    case LEFT: {
                        if ( canMove( hoverPoint, -1, 0 ) ) {
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
    
    private boolean canMove( Point p, int dx, int dy ) {
        boolean retVal = true;
        
        int nx = p.x + dx;
        int ny = p.y + dy;
        
        Square[][] board = GameState.getCurrent().getBoard();
        if ( ( nx >= board[ 0 ].length ) || ( nx < 0 ) ||
                ( ny >= board.length ) || ( ny < 0 ) ) {
            retVal = false;
        } else if ( board[ ny ][ nx ] == null ) {
            retVal = false;
        }
        
        return retVal;
    }

    private void touch( int i, int j ) {
        boolean ItsGoingToBeOkay = true;
        GameState.Direction d = null;
        try {
            Square[][] board = GameState.getCurrent().getBoard();
            
            if ( board[ j ][ i ] instanceof Atom ) {
                
            } else if ( board[ j ][ i ] instanceof Square ) {
                
            }
            
            if ( board[ j ][ i ] instanceof Atom ) {
                try {
                    GameState.getCurrent().select( i, j );
                } catch ( GameException e ) {
                    // this shouldn't ever happen, we checked beforehand
                    assert false;
                }
                
                // set the squares were arrows should be drawn
                this.setArrowSquares();
            } else if ( ( d = arrowSquares.get( new Point( i, j ) ) )
                    != null ) {
                // we clicked on an arrow -> move the selected atom
                Log.d( "Atomix:TouchEvent", "Touched an arrow: " + d );
                
                try {
                    if ( GameState.getCurrent().moveSelected( d ) ) {
                        Log.d( "Atomix:GameState", "You win!" );
                        // we won the level!
                        Toast toast = Toast.makeText( atomix, "You win!",
                                Toast.LENGTH_LONG );
                        toast.show();
                        //@todo handle this betters!
                    }
                } catch ( GameException e ) {
                    // no currently selected atom.  this shouldn't happen. evar.
                    assert false;
                }
                
                // move the hover point to the new location of the Atom
                Atom selected = GameState.getCurrent().getSelected();
                
                Point hoverPoint = GameState.getCurrent().getHoverPoint();
                hoverPoint.set( selected.getX(), selected.getY() );

                // set the squares were arrows should be drawn
                this.setArrowSquares();
                
            }
            
            //hoverPoint = null;
            
            // if only forgetting were
            // this easy for me.
            assert ItsGoingToBeOkay;
            
            // force a redraw
            super.postInvalidate();
            
        } catch ( ArrayIndexOutOfBoundsException e ) {
            // touch out of bounds
        }
    }
    
    private void setArrowSquares() {
        // identify and store all Direction flags to be drawn
        EnumSet<GameState.Direction> directions =
                GameState.getCurrent().getPossibleDirections();
        
        arrowSquares.clear();
        for ( GameState.Direction dir : directions ) {
            int x = GameState.getCurrent().getSelected().getX();
            int y = GameState.getCurrent().getSelected().getY();
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
    
} // AtomicView

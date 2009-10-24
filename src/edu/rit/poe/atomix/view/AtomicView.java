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
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import edu.rit.poe.atomix.AtomicActivity;
import edu.rit.poe.atomix.game.GameException;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Connector;
import edu.rit.poe.atomix.levels.Square;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class AtomicView extends View {
    
    private AtomicActivity atomix;
    
    private GameState gameState;
    
    private Square[][] board;
    
    Map<Point, GameState.Direction> arrowSquares;
    
    /** The point that is currently being hovered over, or <tt>null</tt>. */
    private Point hoverPoint;
    
    private long trackballTime;
    
    private GameState.Direction trackballDir;
    
    private float trackballSum;
    
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
        this.gameState = atomix.getGameState();
        this.board = gameState.getBoard();
        
        // squares that will be drawn as arrows
        arrowSquares = new HashMap<Point, GameState.Direction>();
        
        hoverPoint = new Point( 0, 0 );
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
        int bgcolor = Color.GRAY;
        int fgcolor = Color.parseColor( "#F1E9D9" );
        int colorred = Color.parseColor( "#714444" );
        
        Paint p = new Paint();
        p.setColor( bgcolor );
        
        Rect rect = new Rect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        canvas.drawRect( rect, p );
        
        // the board region is given an 11x11 with 29x29 squares
        
        // determine where to put the board
        
        
        int SQUARES = Math.max( board.length, board[ 0 ].length );
        
        
        
        int MAX_PIXELS = Math.min( canvas.getWidth(), canvas.getHeight() );
        int size = ( int )( MAX_PIXELS / SQUARES );
        Log.d( "SIZE", "Size: " + size );
        
        // translate to create the 1px top/left gap
        canvas.translate( 1, 2 );
        
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
                    if ( board[ j ][ i ] == Square.WALL ) {
                        p.setColor( colorred );
                        canvas.drawRect( sq, p );
                    } else if ( board[ j ][ i ] == Square.EMPTY ) {
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
                        
                        // draw the connectors first
                        for ( Connector c : atom.getConnectors() ) {
                            canvas.save();
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
                            }
                            
                            if ( c.getBond() == Connector.Bond.SINGLE ) {
                                p.setColor( Color.BLACK );
                                canvas.drawLine( 1, 0, 1, -r, p );
                                p.setColor( Color.BLACK );
                                canvas.drawLine( 0, 0, 0, -r, p );
                                p.setColor( Color.BLACK );
                                canvas.drawLine( -1, 0, -1, -r, p );
                            } else if ( c.getBond() == Connector.Bond.DOUBLE ) {
                                p.setColor( Color.BLACK );
                                canvas.drawLine( 3, 0, 3, -r, p );
                                canvas.drawLine( 2, 0, 2, -r, p );
                                
                                p.setColor( Color.BLACK );
                                canvas.drawLine( -2, 0, -2, -r, p );
                                canvas.drawLine( -3, 0, -3, -r, p );
                            }
                            
                            canvas.restore();
                        }
                        
                        // draw the atom circle
                        p.setColor( Color.parseColor( "#" + atom.getColor() ) );
                        int radius = r - 4;
                        canvas.drawCircle( 0, 0, radius, p );
                        
                        // draw the element letter
                        p.setTypeface( Typeface.DEFAULT_BOLD );
                        Rect bounds = new Rect();
                        p.getTextBounds( new char[]{ atom.getElement() }, 0, 1,
                                bounds );
                        int tx = 0 - ( ( bounds.width() / 2 ) + 1 );
                        int ty = 0 + ( bounds.height() / 2 );
                        p.setColor( Color.WHITE );
                        canvas.drawText( Character.toString(
                                atom.getElement() ), tx, ty, p );
                        
                    } else {
                        p.setColor( bgcolor );
                        canvas.drawRect( sq, p );
                    }
                } catch ( Exception e ) {
                    p.setColor( bgcolor );
                }
                
                // does this square as the hover point?
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
        
        if ( dir == GameState.Direction.UP ) {
            // nothing!
        } else if ( dir == GameState.Direction.DOWN ) {
            canvas.rotate( 180.0f );
        } else if ( dir == GameState.Direction.RIGHT ) {
            canvas.rotate( 90.0f );
        } else if ( dir == GameState.Direction.LEFT ) {
            canvas.rotate( -90.0f );
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
        int i = ( int )( event.getX() / 29.0f );
        int j = ( int )( event.getY() / 29.0f );

        // are we setting the hover pointer, or selecting a square
        if ( ( action == MotionEvent.ACTION_DOWN ) ||
                ( action == MotionEvent.ACTION_MOVE ) ) {
            // is this a new hover point?
            if ( ( hoverPoint == null ) || ( !hoverPoint.equals( i, j ) ) ) {
                hoverPoint = new Point( i, j );

                // force a redraw
                super.postInvalidate();
            }
        } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
            Log.d( "TOUCH EVENT", "Selected at " + i + ", " + j );

            // select the currently hovered square
            touch( i, j );
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
            if ( board[ j ][ i ] instanceof Atom ) {
                try {
                    gameState.select( i, j );
                } catch ( GameException e ) {
                    // this shouldn't ever happen, we checked beforehand
                    assert false;
                }
                
                // set the squares were arrows should be drawn
                this.setArrowSquares();
            } else if ( ( d = arrowSquares.get( new Point( i, j ) ) )
                    != null ) {
                // we clicked on an arrow -> move the selected atom
                Log.d( "TOUCH EVENT", "Touched an arrow: " + d );
                
                try {
                    gameState.moveSelected( d );
                } catch ( GameException e ) {
                    // no currently selected atom.  this should never happen.
                    assert false;
                }
                
                // move the hover point to the new location of the Atom
                Atom selected = gameState.getSelected();
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
                gameState.getPossibleDirections();
        
        arrowSquares.clear();
        for ( GameState.Direction dir : directions ) {
            int x = gameState.getSelected().getX();
            int y = gameState.getSelected().getY();
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

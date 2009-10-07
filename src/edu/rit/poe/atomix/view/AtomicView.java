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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import edu.rit.poe.atomix.AtomicActivity;
import edu.rit.poe.atomix.R;
import edu.rit.poe.atomix.game.GameException;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.Atom;
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
    
    /** The point that is currently being hovered over via the trackpad. */
    private Point trackPoint;
    
    /**
     * Constructs a new <tt>AtomicView</tt>.
     * 
     * @param   atomix  the Atomix Activity that launched this view
     */
    public AtomicView( AtomicActivity atomix ) {
        super( atomix );
        this.atomix = atomix;
        this.gameState = atomix.getGameState();
        this.board = gameState.getBoard();
        
        // squares that will be drawn as arrows
        arrowSquares = new HashMap<Point, GameState.Direction>();
        
        super.setScrollContainer( false );
        super.setClickable( true );
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
        
        Paint p = new Paint();
        p.setColor( bgcolor );
        
        Rect rect = new Rect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        
        canvas.drawRect( rect, p );
        
        int colorred = Color.parseColor( "#714444" );
        
        // side area
        
        rect = new Rect( 320, 0, canvas.getWidth(), canvas.getHeight() );
        p.setColor( Color.BLACK );
        canvas.drawRect( rect, p );
        
        Bitmap bitmap = BitmapFactory.decodeResource( atomix.getResources(),
                R.drawable.atomix_logo );
        
        Rect prect = new Rect( 320, 0, canvas.getWidth(), 40 );
        
        canvas.drawBitmap( bitmap, null, prect, null );
        
        int SQUARES = Math.max( board.length, board[ 0 ].length );
        int MAX_PIXELS = Math.min( canvas.getWidth(), canvas.getHeight() );
        int size = ( int )( MAX_PIXELS / SQUARES );
        Log.i( "SIZE", "Size: " + size );
        
        int tx = ( canvas.getHeight() - ( size * board[ 0 ].length ) ) / 2;
        int ty = ( canvas.getHeight() - ( size * board.length ) ) / 2;
        
        canvas.translate( ( tx + 1 ), ( ty + 1 ) );
        
        GameState.Direction dir = null;
        for ( int i = 0; i < board[ 0 ].length; i++ ) {
            for ( int j = 0 ; j < board.length; j++ ) {
                int left = i * size;
                int top = j * size;
                
                // translate to this square area
                canvas.translate( left, top );
                
                Rect sq = new Rect( 0, 0, size, size );
                p.setColor( bgcolor );
                canvas.drawRect( sq, p );
                
                // draw the square color
                sq = new Rect( 1, 1, ( size - 1 ), ( size - 1 ) );
                
                try {
                    if ( board[ j ][ i ] == Square.WALL ) {
                        p.setColor( colorred );
                        canvas.drawRect( sq, p );
                    } else if ( board[ j ][ i ] == Square.EMPTY ) {
                        // check whether this square should represent an arrow
                        if ( ( dir = arrowSquares.get( new Point( i, j ) ) )
                                != null ) {
                            
                            p.setColor( fgcolor );
                            canvas.drawRect( sq, p );
                            
                            int cx = sq.width() / 2;
                            int cy = sq.height() / 2;
                            
                            switch ( dir ) {
                                case UP: {
                                    
                                    drawArrow( canvas, sq );
                                    
                                } break;
                                case DOWN: {
                                    canvas.translate( cx, cy );
                                    canvas.rotate( 180.0f );
                                    canvas.translate( -cx, -cy );
                                    
                                    drawArrow( canvas, sq );
                                    
                                    canvas.translate( cx, cy );
                                    canvas.rotate( -180.0f );
                                    canvas.translate( -cx, -cy );
                                } break;
                                case LEFT: {
                                    canvas.translate( cx, cy );
                                    canvas.rotate( 270.0f );
                                    canvas.translate( -cx, -cy );
                                    
                                    drawArrow( canvas, sq );
                                    
                                    canvas.translate( cx, cy );
                                    canvas.rotate( -270.0f );
                                    canvas.translate( -cx, -cy );
                                } break;
                                case RIGHT: {
                                    canvas.translate( cx, cy );
                                    canvas.rotate( 90.0f );
                                    canvas.translate( -cx, -cy );
                                    
                                    drawArrow( canvas, sq );
                                    
                                    canvas.translate( cx, cy );
                                    canvas.rotate( -90.0f );
                                    canvas.translate( -cx, -cy );
                                } break;
                            }
                            
                            
                        } else {
                            p.setColor( fgcolor );
                            canvas.drawRect( sq, p );
                        }
                    } else if ( board[ j ][ i ] instanceof Atom ) {
                        p.setColor( fgcolor );
                        canvas.drawRect( sq, p );
                        
                        int cx = tx + ( size / 2 );
                        int cy = ty;
                        int r = ( size - 6 ) / 2;
                        
                        if ( gameState.getSelected() == board[ j ][ i ] ) {
                            p.setColor( Color.BLUE );
                        } else {
                            p.setColor( Color.RED );
                        }
                        
                        canvas.drawCircle( cx, cy, r, p );
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
                
                // ul
                Rect corner = new Rect( 1, 1, 2, 2 );
                canvas.drawRect( corner, p );
                
                // ur
                corner = new Rect( ( size - 2 ), 1, ( size - 1 ), 2 );
                canvas.drawRect( corner, p );
                
                // ll
                corner = new Rect( 1, ( size - 2 ), 2, ( size - 1 ) );
                canvas.drawRect( corner, p );
                
                // lr
                corner = new Rect( ( size - 2 ), ( size - 2 ), ( size - 1 ),
                        ( size - 1 ) );
                canvas.drawRect( corner, p );
                
                
                // pop the matrix stack :)
                canvas.translate( -left, -top );
            }
        }
    }
    
    private static void drawArrow( Canvas canvas, Rect sq ) {
        
        int w = sq.width();
        int h = sq.height();
        
        Path path = new Path();
        
        path.moveTo( 3, ( h - 3 ) );
        path.lineTo( ( w - 3 ), ( h - 3 ) );
        path.lineTo( ( w / 2 ), 3 );
        
        Paint paint = new Paint();
        paint.setColor( Color.GREEN );
        canvas.drawPath( path, paint );
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
        boolean ItsGoingToBeOkay = true;
        int action = event.getAction();
        int i = ( int )( event.getX() / 29.0f );
        int j = ( int )( event.getY() / 29.0f );
        
        // are we setting the hover pointer, or selecting a square
        if ( ( action == MotionEvent.ACTION_DOWN ) ||
                ( action == MotionEvent.ACTION_MOVE ) ) {
            // is this a new hover point?
            if ( ( hoverPoint == null ) || ( ! hoverPoint.equals( i, j ) ) ) {
                hoverPoint = new Point( i, j );
                
                // force a redraw
                super.postInvalidate();
            }
        } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
            Log.i( "TOUCH EVENT", "Selected at " + i + ", " + j );
            GameState.Direction d = null;
            
            if ( board[ j ][ i ] instanceof Atom ) {
                try {
                    gameState.select( i, j );
                } catch ( GameException e ) {
                    // this shouldn't ever happen, we checked beforehand
                    assert false;
                }
                
                // set the squares were arrows should be drawn
                this.setArrowSquares();
            } else if (
                    ( d = arrowSquares.get( new Point( i, j ) ) ) != null ) {
                // we clicked on an arrow -> move the selected atom
                Log.d( "TOUCH EVENT", "Touched an arrow: " + d );
                
                try {
                    gameState.moveSelected( d );
                } catch ( GameException e ) {
                    // no currently selected atom.  this should never happen.
                    assert false;
                }
                
                // set the squares were arrows should be drawn
                this.setArrowSquares();
                
                // @todo implement a sliding display method
            }
            
            hoverPoint = null;
            // if only forgetting were
            // this easy for me.
            assert ItsGoingToBeOkay;
            
            // force a redraw
            super.postInvalidate();
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
        
        float y = Math.abs( event.getY() );
        float x = Math.abs( event.getX() );
        
        if ( y > x ) {
            if ( event.getY() < 0 ) {
                Log.d( "TRACKBALL", "Y up" );
            } else if ( event.getY() > 0 ) {
                Log.d( "TRACKBALL", "Y down" );
            }
        } else {
            if ( event.getX() < 0 ) {
                Log.d( "TRACKBALL", "X left" );
            } else if ( event.getX() > 0 ) {
                Log.d( "TRACKBALL", "X right" );
            }
        }
        return true;
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
                case RIGHT: {
                    x++;
                } break;
                case LEFT: {
                    x--;
                } break;
            }
            arrowSquares.put( new Point( x, y ), dir );
        }
    }
    
} // AtomicView

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
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.rit.poe.atomix.AtomicActivity;
import edu.rit.poe.atomix.R;
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Square;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class AtomicView extends View {
    
    private AtomicActivity context;
    
    /**
     * Constructs a new <tt>AtomicView</tt>.
     * 
     * @param   context     the activity context that launched this view
     */
    public AtomicView( AtomicActivity context ) {
        super( context );
        this.context = context;
        
        super.setScrollContainer( false );
        super.setClickable( true );
    }
    
    /**
     * Draw this view.
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
        
        Bitmap bitmap = BitmapFactory.decodeResource( context.getResources(),
                R.drawable.atomix_logo );
        
        Rect prect = new Rect( 320, 0, canvas.getWidth(), 40 );
        
        canvas.drawBitmap( bitmap, null, prect, null );
        
        // board
        
        Square[][] board = context.getGameState().getBoard();
        
        int SQUARES = Math.max( board.length, board[ 0 ].length );
        int MAX_PIXELS = Math.min( canvas.getWidth(), canvas.getHeight() );
        int size = ( int )( MAX_PIXELS / SQUARES );
        Log.i( "SIZE", "Size: " + size );
        
        int tx = ( canvas.getHeight() - ( size * board[ 0 ].length ) ) / 2;
        int ty = ( canvas.getHeight() - ( size * board.length ) ) / 2;
        
        canvas.translate( ( tx + 1 ), ( ty + 1 ) );
        
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
                        p.setColor( fgcolor );
                        canvas.drawRect( sq, p );
                    } else if ( board[ j ][ i ] instanceof Atom ) {
                        p.setColor( fgcolor );
                        canvas.drawRect( sq, p );
                        
                        int cx = tx + ( size / 2 );
                        int cy = ty;
                        int r = ( size - 6 ) / 2;
                        
                        if ( context.getGameState().getSelected() ==
                                board[ j ][ i ] ) {
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
        
        p.setColor( Color.RED );
        
        canvas.drawLine( x0, y0, x0 + 1, y0 + 1, p );
    }
    
    int x0 = 0;
    
    int y0 = 0;
    
    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        if ( event.getAction() == MotionEvent.ACTION_UP ) {
            int x = ( int )event.getX();
            int y = ( int )event.getY();
            
            x0 = x;
            y0 = y;
            
            int i = x / 29;
            int j = y / 29;
            
            Log.i( "TOUCH EVENT", "Touched at " + i + ", " + j );
            try {
                context.getGameState().select( i, j );
            } catch ( Exception e ) {
                
            }
            super.postInvalidate();
        }
        return true;
    }
    
} // AtomicView

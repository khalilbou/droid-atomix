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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.rit.poe.atomix.R;
import edu.rit.poe.atomix.levels.Level;
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Square;
import java.io.InputStream;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class AtomicView extends View {
    
    private Level level;
    
    private Context context;
    
    /**
     * Constructs a new <tt>AtomicView</tt>.
     * 
     * @param   context     the activity context that launched this view
     */
    public AtomicView( Context context ) {
        super( context );
        this.context = context;
        
        super.setScrollContainer( false );
        super.setClickable( true );
        
        
        
        field = new Box[ 13 ][ 13 ];
        for ( int i = 0; i < 13; i++ ) {
            for ( int j = 0 ; j < 13; j++ ) {
                field[ i ][ j ] = new Box( i, j, Color.BLUE );
            }
        }
        
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open( "levels/level1.level" );
            level = Level.loadLevel( is );
        } catch ( Exception e ) {
            // ignore
        }
        
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
        
        Square[][] board = level.getBoard();
        
        int SQUARES = Math.max( board.length, board[ 0 ].length );
        int size = ( int )( canvas.getHeight() / SQUARES );
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
                        
                        p.setColor( Color.RED );
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
        
    }
    
    private Box[][] field;
    
    class Box {
        
        private int color;
        
        private int x;
        
        private int y;
        
        Box( int x, int y, int color ) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
        
    }
    
    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        if ( event.getAction() == MotionEvent.ACTION_UP ) {
            int x = ( int )event.getX();
            int y = ( int )event.getY();
            
            int i = x / 29;
            int j = y / 29;
            
            Log.i( "TOUCH EVENT", "Touched at " + i + ", " + j );
            try {
                field[ i ][ j ].color = Color.RED;
            } catch ( Exception e ) {
                // ArrayIndexOutOfBoundsExceptionIgnore
            }
            super.postInvalidate();
        }
        return true;
    }
    
} // AtomicView

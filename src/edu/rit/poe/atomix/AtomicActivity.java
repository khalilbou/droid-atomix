/*
 * AtomicActivity.java
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

package edu.rit.poe.atomix;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import edu.rit.poe.atomix.game.GameDatabase;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.view.AtomicView;
import java.io.IOException;

/**
 *
 * @author poe9514
 */
public class AtomicActivity extends Activity {
    
    private AtomicView view;
    
    /**
     * Called when the activity is first created.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        
        super.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        
        // remove the titlebar (it's not needed)
        super.requestWindowFeature( Window.FEATURE_NO_TITLE );
        
        view = new AtomicView( this );
        
        super.setContentView( view );
    }
    
    /**
     * Called when the trackball is moved.
     * 
     * @param   event   the trackball event
     * 
     * @return          <tt>true</tt>, since the event was handled
     */
    @Override
    public boolean onTrackballEvent( MotionEvent event ) {
        // pass-through to AtomicView
        return view.onTrackballEvent( event );
    }
    
    @Override
    protected void onSaveInstanceState( Bundle icicle ) {
        super.onSaveInstanceState( icicle );
        Log.d( "DROID_ATOMIX", "onSaveInstanceState() called" );
        
        // save the game state
        icicle.putSerializable( "game_state", GameState.getCurrent() );
        
        try {
            Log.d( "DROID_ATOMIX", "Saving state: "
                    + GameState.getCurrent().getUser() );
            GameDatabase.setLast( GameState.getCurrent() );
            GameDatabase.save( this );
        } catch ( IOException ex ) {
            Log.e( "DROID_ATOMIX", Log.getStackTraceString( ex ) );
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        
    }
    
    @Override
    protected void onRestoreInstanceState( Bundle icicle ) {
        super.onRestoreInstanceState( icicle );
        Log.d( "DROID_ATOMIX", "onRestoreInstanceState() called" );
        
        // restore game state
        GameState.setCurrent(
                ( GameState )icicle.getSerializable( "game_state" ) );
    }
    
} // AtomicActivity

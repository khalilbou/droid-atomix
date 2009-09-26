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
import android.view.Window;
import android.view.WindowManager;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.game.User;
import edu.rit.poe.atomix.levels.LevelManager;
import edu.rit.poe.atomix.view.AtomicView;

/**
 *
 * @author poe9514
 */
public class AtomicActivity extends Activity {
    
    private GameState gameState;
    
    /**
     * Called when the activity is first created.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        
        super.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
        
        // remove the titlebar (it's not needed)
        super.requestWindowFeature( Window.FEATURE_NO_TITLE );
        super.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
        // initialize the level manager
        LevelManager lm = LevelManager.getInstance();
        lm.init( this );
        
        // setup the game state
        gameState = new GameState( new User() );
        
        super.setContentView( new AtomicView( this ) );
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
} // AtomicActivity

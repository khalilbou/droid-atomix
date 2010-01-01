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
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import edu.rit.poe.atomix.db.AtomixDbAdapter;
import edu.rit.poe.atomix.db.Game;
import edu.rit.poe.atomix.game.GameController;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.view.AtomicView;

/**
 * This class is the main <tt>Activity</tt> for managing the Atomix gameplay.
 * All persistence operations for game state are handled by this class.
 * 
 * @author  Peter O. Erickson
 * 
 * @version $Id$
 */
public class AtomicActivity extends Activity {
    
    public static final int REDRAW_VIEW = 0x1;
    
    public static final int WIN_LEVEL = 0x2;
    
    public static final int MENU_ITEM_GOAL = 0x00;
    
    public static final int MENU_ITEM_LEVELS = 0x01;
    
    public static final int MENU_ITEM_USER_INFO = 0x02;
    
    public static final int MENU_ITEM_MAIN_MENU = 0x03;
    
    public static final int MENU_ITEM_QUIT = 0x04;
    
    private AtomicView view;
    
    private AtomixDbAdapter db;
    
    private GameState gameState;
    
    private Handler viewHandler = new Handler() {
        @Override 
        public void handleMessage( Message msg ) {
            if ( msg.what == REDRAW_VIEW ) {
                
                if ( msg.obj != null ) {
                    Rect rect = ( Rect )msg.obj;
                    view.invalidate( rect );
                } else {
                    view.invalidate();
                }
            } else if ( msg.what == WIN_LEVEL ) {
                
                Toast toast = Toast.makeText( AtomicActivity.this, "You win!",
                        Toast.LENGTH_LONG );
                toast.show();
                // @todo handle win condition betters!
            }
            super.handleMessage( msg );
        }
    };
    
    /**
     * Called when the activity is first created.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Log.d( "DROID_ATOMIX", "AtomixActivity.onCreate() was called." );
        
        // if phone is landscape, make fullscreen!!!
        Resources resources = super.getResources();
        Configuration conf = resources.getConfiguration();
        if ( conf.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            this.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }
        
        // remove the titlebar (it's not needed)
        super.requestWindowFeature( Window.FEATURE_NO_TITLE );
        
        // create the database connector
        db = new AtomixDbAdapter( this );
        db.open();
        
        // get the "extras" bundle with the GameState
        Bundle extras = super.getIntent().getExtras();
        gameState =
                ( GameState )extras.getSerializable( GameState.GAME_STATE_KEY );
        
        view = new AtomicView( this, gameState );
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
    
    public void redrawView( Rect rect ) {
        Log.d( "ATOMIX_ACTIVITY", "Redraw view" );
        Message msg = new Message();
        msg.what = REDRAW_VIEW;
        msg.obj = rect;
        viewHandler.sendMessage( msg );
    }
    
    public void winLevel() {
        Message msg = new Message();
        msg.what = WIN_LEVEL;
        viewHandler.sendMessage( msg );
        
        // @todo switch level now!!
        Game newLevel = GameController.newLevel( gameState.getUser(), 2 );
        GameState newGameState = new GameState( gameState.getUser(), newLevel );
        
        // if we won the previous level, save it and move the game pointer in
        // the database
        
        gameState = newGameState;
        
        view.setGameState( gameState );
        
        redrawView( null );
    }
    
    /**
     * Create the options menu.
     * 
     * @param   menu    the application menu to add options to
     * 
     * @return          whether to display the menu on Menu press
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // add the menu items!
        MenuItem item = menu.add( Menu.NONE, MENU_ITEM_GOAL, Menu.NONE,
                "Goal Molecule" );
        item.setIcon( android.R.drawable.ic_menu_zoom );
        
        item = menu.add( Menu.NONE, MENU_ITEM_LEVELS, Menu.NONE, "Levels" );
        item.setIcon( android.R.drawable.ic_menu_compass );
        
        item = menu.add( Menu.NONE, MENU_ITEM_USER_INFO, Menu.NONE,
                "User Info" );
        item.setIcon( android.R.drawable.ic_menu_info_details );
        
        item = menu.add( Menu.NONE, MENU_ITEM_MAIN_MENU, Menu.NONE,
                "Main Menu" );
        item.setIcon( android.R.drawable.ic_menu_more );
        
        item = menu.add( Menu.NONE, MENU_ITEM_QUIT, Menu.NONE, "Quit" );
        item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        // @todo nothing to do here, pretty sure...
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // switch on the selected menu item
        switch ( item.getItemId() ) {
            
            case MENU_ITEM_GOAL: {
                
            } break;
            
            case MENU_ITEM_LEVELS: {
                Intent i = new Intent( this, LevelListActivity.class );
                Bundle extras = new Bundle();
                extras.putSerializable( GameState.GAME_STATE_KEY, gameState );
                i.putExtras( extras );
                super.startActivity( i );
                
            } break;
            
            case MENU_ITEM_USER_INFO: {
                
            } break;
            
            case MENU_ITEM_MAIN_MENU: {
                // go back to the main menu after saving
                // @todo save here?
                super.setResult( MenuActivity.GAME_RESULT_MAIN_MENU );
                super.finish();
            } break;
            
            case MENU_ITEM_QUIT: {
                // quit the game entirely after saving
                // @todo save here?
                super.setResult( MenuActivity.GAME_RESULT_QUIT );
                super.finish();
            } break;
            
        }
        
        return true;
    }
    
    @Override
    public void onConfigurationChanged( Configuration conf ) {
        super.onConfigurationChanged( conf );
        
        Log.d( "DROID_ATOMIX",
                "AtomicActivity.onConfigurationChanged() called" );
        
        // if we're sideways, go fullscreen
        if ( conf.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            this.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );
        } else {
            this.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }
    }
    
    @Override
    protected void onSaveInstanceState( Bundle icicle ) {
        super.onSaveInstanceState( icicle );
        Log.d( "DROID_ATOMIX", "onSaveInstanceState() called" );
        
        // save the game state
        icicle.putSerializable( GameState.GAME_STATE_KEY, gameState );
        
        if ( db == null ) {
            db = new AtomixDbAdapter( this ).open();
        }
        
        db.update( gameState.getUser() );
        db.update( gameState.getGame() );
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d( "DROID_ATOMIX", "onPause() called" );
        
        if ( db == null ) {
            db = new AtomixDbAdapter( this ).open();
        }
        
        db.update( gameState.getUser() );
        db.update( gameState.getGame() );
        
        // close the connection to the database
        db.close();
        db = null;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d( "DROID_ATOMIX", "AtomixActivity.onResume() was called." );
        
        // make sure the view has the right GameState
        view.setGameState( gameState );
    }
    
    @Override
    protected void onRestoreInstanceState( Bundle icicle ) {
        super.onRestoreInstanceState( icicle );
        Log.d( "DROID_ATOMIX", "onRestoreInstanceState() called" );
        
        // restore game state
        gameState =
                ( GameState )icicle.getSerializable( GameState.GAME_STATE_KEY );
    }
    
} // AtomicActivity

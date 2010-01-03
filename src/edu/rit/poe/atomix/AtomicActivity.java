/*
 * AtomicActivity.java
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
import edu.rit.poe.atomix.db.User;
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
    
    public static final int MENU_ITEM_UNDO = 0x02;
    
    public static final int MENU_ITEM_MAIN_MENU = 0x03;
    
    public static final int MENU_ITEM_QUIT = 0x04;
    
    private AtomicView view;
    
    private AtomixDbAdapter db;
    
    private GameState gameState;
    
    private MenuItem undo;
    
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
        db = new AtomixDbAdapter( this ).open();
        
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
        
        // save the old game as finished
        User user = gameState.getUser();
        Game game = gameState.getGame();
        game.setFinished( true );
        db.update( game );
        
        // @todo switch level now!!
        Game newLevel = GameController.newLevel( user, game.getLevel() + 1 );
        user.setCurrentGame( newLevel );
        db.insert( newLevel );
        db.update( user );
        
        GameState newGameState = new GameState( user, newLevel );
        
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
        item.setIcon( R.drawable.levels_cclicense );
        
        undo = menu.add( Menu.NONE, MENU_ITEM_UNDO, Menu.NONE,
                "Undo" );
        undo.setIcon( R.drawable.undo );
        
        item = menu.add( Menu.NONE, MENU_ITEM_MAIN_MENU, Menu.NONE,
                "Main Menu" );
        item.setIcon( R.drawable.options );
        
        item = menu.add( Menu.NONE, MENU_ITEM_QUIT, Menu.NONE, "Quit" );
        item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        // check to see if we can undo a move
        undo.setEnabled( GameController.canUndo( gameState ) );
        
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
            
            case MENU_ITEM_UNDO: {
                // undo the last move
                GameController.undo( gameState );
            } break;
            
            case MENU_ITEM_MAIN_MENU: {
                // go back to the main menu (save will happen in onPause())
                super.setResult( MenuActivity.GAME_RESULT_MAIN_MENU );
                super.finish();
            } break;
            
            case MENU_ITEM_QUIT: {
                // quit the game entirely (save will happen in onPause())
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
        
        db.update( gameState.getUser() );
        db.update( gameState.getGame() );
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d( "DROID_ATOMIX", "onPause() called" );
        
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
        
        // turn the database adapter back on
        if ( db == null ) {
            db = new AtomixDbAdapter( this ).open();
        }
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

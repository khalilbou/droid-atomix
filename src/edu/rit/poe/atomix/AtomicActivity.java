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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import edu.rit.poe.atomix.db.AtomixDbAdapter;
import edu.rit.poe.atomix.db.Game;
import edu.rit.poe.atomix.db.User;
import edu.rit.poe.atomix.game.GameController;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.LevelManager;
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
    
    /** The tag for all log messages from this activity. */
    public static final String LOG_TAG = AtomicActivity.class.getName();
    
    /**
     * The request code used to request an answer from the level list activity,
     * used to either start a new level or remain in the current game.
     */
    public static final int LEVEL_LIST_REQUEST_CODE = 0x0;
    
    /** The result code from the level list activity to start a new level. */
    public static final int START_NEW_LEVEL_RESULT_CODE = 0x1;
    
    /** An event code for this activity's message handler to redraw the view. */
    public static final int EVENT_REDRAW_VIEW = 0x1;
    
    /** An event code for this activity's message handler to win the game. */
    public static final int EVENT_WIN_LEVEL = 0x2;
    
    /** The code for the 'View Goal' menu item in the context menu. */
    public static final int MENU_ITEM_GOAL = 0x00;
    
    /** The code for the 'Level List Activity' menu item in the context menu. */
    public static final int MENU_ITEM_LEVELS = 0x01;
    
    /** The code for the 'Undo' menu item in the context menu. */
    public static final int MENU_ITEM_UNDO = 0x02;
    
    /** The code for the 'Next Level' menu item in the context menu. */
    public static final int MENU_ITEM_NEXT_LEVEL = 0x05;
    
    /** The code for the 'Previous Level' menu item in the context menu. */
    public static final int MENU_ITEM_PREVIOUS_LEVEL = 0x06;
    
    /** The code for the 'Restart Level' menu item in the context menu. */
    public static final int MENU_ITEM_RESTART_LEVEL = 0x07;
    
    /** The code for the 'Main Menu' menu item in the context menu. */
    public static final int MENU_ITEM_MAIN_MENU = 0x03;
    
    /** The code for the 'Quit' menu item in the context menu." */
    public static final int MENU_ITEM_QUIT = 0x04;
    
    /** The ID of the 'Confirm Lose Unsaved Game' dialog. */
    public static final int DIALOG_CONFIRM_UNSAVED_LEVEL = 0x0;
    
    /** The ID of the 'Confirm Overwrite Existing Game' dialog. */
    public static final int DIALOG_CONFIRM_OVERWRITE_LEVEL = 0x1;
    
    /** The ID of the 'Win Level' dialog. */
    public static final int DIALOG_WIN_LEVEL = 0x2;
    
    /** The ID of the 'Goal Molecule' dialog. */
    public static final int DIALOG_GOAL_MOLECULE = 0x3;
    
    /** The <tt>View</tt> for this activity, to display the game board. */
    private AtomicView view;
    
    /** The database adapter to persist and query game state. */
    private AtomixDbAdapter db;
    
    /** The current game's state information. */
    private GameState gameState;
    
    /** The 'Undo' menu item in the context menu. */
    private MenuItem undoMenuItem;
    
    /** The 'Next Level' menu item in the context menu. */
    private MenuItem nextMenuItem;
    
    /** The 'Previous Level' menu item in the context menu. */
    private MenuItem prevMenuItem;
    
    /**
     * This integer is the level number that is to be started, assuming all
     * dialog confirmations are passed.
     */
    private int pendingLevel;
    
    /** A handler for view-related events.  */
    private Handler viewHandler = new Handler() {
        @Override 
        public void handleMessage( Message msg ) {
            if ( msg.what == EVENT_REDRAW_VIEW ) {
                
                if ( msg.obj != null ) {
                    Rect rect = ( Rect )msg.obj;
                    view.invalidate( rect );
                } else {
                    view.invalidate();
                }
            } else if ( msg.what == EVENT_WIN_LEVEL ) {
                showDialog( DIALOG_WIN_LEVEL );
            }
            super.handleMessage( msg );
        }
    };
    
    // ===== Lifecycle Methods =====
    
    /**
     * Called when the activity is first created.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Log.d( LOG_TAG, "onCreate() was called." );
        
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
     * Called to save activity state to the specified bundle.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    protected void onSaveInstanceState( Bundle icicle ) {
        super.onSaveInstanceState( icicle );
        Log.d( LOG_TAG, "onSaveInstanceState() called" );
        
        // save the game state
        icicle.putSerializable( GameState.GAME_STATE_KEY, gameState );
        
        db.update( gameState.getUser() );
        db.update( gameState.getGame() );
    }
    
    /**
     * Called to pause this activity cleanly.
     * <p>
     * The game timer is stopped and all game state is persisted to the
     * database.  The connection to the database is broken.
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d( LOG_TAG, "onPause() called" );
        
        // stop the playing timer
        GameController.stopTimer( gameState );
        
        db.update( gameState.getUser() );
        db.update( gameState.getGame() );
        
        // close the connection to the database
        db.close();
        db = null;
    }
    
    /**
     * Called to resume this activity cleanly.
     * <p>
     * All game state is set and the game timer is started.  A connection is
     * made to the game database.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d( LOG_TAG, "onResume() was called." );
        
        // make sure the view has the right GameState
        view.setGameState( gameState );
        
        // start the playing timer
        GameController.startTimer( gameState );
        
        // turn the database adapter back on
        if ( db == null ) {
            db = new AtomixDbAdapter( this ).open();
        }
    }
    
    /**
     * Restores the specified frozen state of the game.
     * 
     * @param   icicle  the bundle of frozen state information
     */
    @Override
    protected void onRestoreInstanceState( Bundle icicle ) {
        super.onRestoreInstanceState( icicle );
        Log.d( LOG_TAG, "onRestoreInstanceState() called" );
        
        // restore game state
        gameState =
                ( GameState )icicle.getSerializable( GameState.GAME_STATE_KEY );
    }
    
    /**
     * Called when an activity finished and returns to this one.
     * <p>
     * This method is used to trigger a new level from the Level List Activity.
     * 
     * @param   requestCode     the activity request code
     * @param   resultCode      the activity result code
     * @param   data            additional data returned from the activity
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode,
            Intent data ) {
        if ( requestCode == LEVEL_LIST_REQUEST_CODE ) {
            if ( resultCode == START_NEW_LEVEL_RESULT_CODE ) {
                // execute the confirmations before starting the new level
                Bundle extras = data.getExtras();
                int level = extras.getInt( Game.LEVEL_KEY );
                this.confirmAndStartLevel( level );
                
            } else {
                // continue playing this level
            }
        }
    }
    
    // ===== Other Android Methods =====
    
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
    
    /**
     * This method handles configuration changes to the phone, such as a change
     * to the orientation.
     * 
     * @param   conf    the new configuration
     */
    @Override
    public void onConfigurationChanged( Configuration conf ) {
        super.onConfigurationChanged( conf );
        Log.d( LOG_TAG, "onConfigurationChanged() called" );
        
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
    
    // ===== Menu Methods =====
    
    /**
     * Create the options menu.
     * 
     * @param   menu    the application menu to add options to
     * 
     * @return          always <tt>true</tt>, to display the menu on Menu press
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // add the menu items!
        MenuItem item = null;
        
        // the context menu:
        // [PREVIOUS]      [LEVELS]        [NEXT]
        //   [UNDO]     [GOAL MOLECULE]    [MORE]
        
        // Previous Level
        prevMenuItem= menu.add( Menu.NONE, MENU_ITEM_PREVIOUS_LEVEL, Menu.NONE,
                R.string.menu_previous );
        prevMenuItem.setIcon( R.drawable.arrow_left );
        
        // Levels
        item = menu.add( Menu.NONE, MENU_ITEM_LEVELS, Menu.NONE,
                R.string.menu_levels );
        item.setIcon( R.drawable.levels_cclicense );
        
        // Next Level
        nextMenuItem= menu.add( Menu.NONE, MENU_ITEM_NEXT_LEVEL, Menu.NONE,
                R.string.menu_next );
        nextMenuItem.setIcon( R.drawable.arrow_right );
        
        // Undo
        undoMenuItem = menu.add( Menu.NONE, MENU_ITEM_UNDO, Menu.NONE,
                R.string.menu_undo );
        undoMenuItem.setIcon( R.drawable.undo );
        
        // Goal Molecule
        item = menu.add( Menu.NONE, MENU_ITEM_GOAL, Menu.NONE,
                R.string.menu_goal );
        item.setIcon( android.R.drawable.ic_menu_zoom );
        
        // Restart Level
        item = menu.add( Menu.NONE, MENU_ITEM_RESTART_LEVEL, Menu.NONE,
                R.string.menu_restart );
        
        // Main Menu
        item = menu.add( Menu.NONE, MENU_ITEM_MAIN_MENU, Menu.NONE,
                R.string.menu_main );
        
        // Quit Game
        item = menu.add( Menu.NONE, MENU_ITEM_QUIT, Menu.NONE,
                R.string.menu_quit );
        
        return true;
    }
    
    /**
     * Prepares the options menu before showing it to the user.
     * 
     * @param   menu    the menu to be prepared
     * 
     * @return          always <tt>true</tt>, since this was handled
     */
    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        LevelManager levelManager = LevelManager.getInstance();
        
        // check to see if we can undo a move
        undoMenuItem.setEnabled( GameController.canUndo( gameState ) );
        
        // check on the next and previous level menu items
        prevMenuItem.setEnabled(
                levelManager.hasLevel( gameState.getLevel() - 1 ) );
        nextMenuItem.setEnabled(
                levelManager.hasLevel( gameState.getLevel() + 1 ) );
        
        
        return true;
    }
    
    /**
     * Handles a menu item being selected from the options menu.
     * 
     * @param   item    the item that was clicked
     * 
     * @return          always <tt>true</tt>, since the event was handled
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // switch on the selected menu item
        switch ( item.getItemId() ) {
            
            case MENU_ITEM_GOAL: {
                showDialog( DIALOG_GOAL_MOLECULE );
            } break;
            
            case MENU_ITEM_LEVELS: {
                // show the level list activity
                startLevelListActivity();
                
            } break;
            
            case MENU_ITEM_UNDO: {
                // undo the last move
                GameController.undo( gameState );
                
                // a redraw is needed immediately after an undo
                redrawView( null );
            } break;
            
            case MENU_ITEM_RESTART_LEVEL: {
                // confirm that we want to restart this level
                int level = gameState.getLevel();
                this.confirmAndStartLevel( level );
                
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
            
            case MENU_ITEM_NEXT_LEVEL: {
                // confirm and launch the next level
                this.confirmAndStartLevel( gameState.getLevel() + 1 );
                
            } break;
            
            case MENU_ITEM_PREVIOUS_LEVEL: {
                // confirm and launch the previous level
                this.confirmAndStartLevel( gameState.getLevel() - 1 );
                
            } break;
        }
        
        return true;
    }
    
    // ===== Dialog Methods =====
    
    /**
     * Called to create dialogs for this activity.
     * 
     * @param   id  the integer ID of the dialog to be created
     * 
     * @return      the newly created <tt>Dialog</tt> of the specified type
     * 
     * @see         DIALOG_CONFIRM_UNSAVED_LEVEL
     * @see         DIALOG_CONFIRM_OVERWRITE_LEVEL
     * @see         DIALOG_WIN_LEVEL
     * @see         DIALOG_GOAL_MOLECULE
     */
    @Override
    protected Dialog onCreateDialog( int id ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        
        if ( id == DIALOG_CONFIRM_UNSAVED_LEVEL ) {
            builder.setTitle( R.string.unsaved_dialog_title );
            builder.setMessage( R.string.unsaved_dialog_text );
            builder.setCancelable( false );
            builder.setPositiveButton( R.string.confirm_yes,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    // check for level overwrite
                    checkOverwriteOldLevel();
                }
            } );
            builder.setNegativeButton( R.string.confirm_no,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    dialog.dismiss();
                }
            });
            
        } else if ( id == DIALOG_CONFIRM_OVERWRITE_LEVEL ) {
            builder.setTitle( R.string.overwrite_dialog_title );
            builder.setMessage( R.string.overwrite_dialog_text );
            builder.setCancelable( false );
            builder.setPositiveButton( R.string.confirm_yes,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    // confirmed!  start the level
                    AtomicActivity.this.startLevel( pendingLevel );
                }
            } );
            builder.setNegativeButton( R.string.confirm_no,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    // nevermind!
                    dialog.dismiss();
                }
            });
            
        } else if ( id == DIALOG_WIN_LEVEL ) {
            builder.setTitle( R.string.win_dialog_title );
            builder.setMessage( "" ); // placeholder to be populated later
            // positive for the left side
            builder.setPositiveButton( R.string.win_dialog_levels_button,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    // start the level list activity
                    startLevelListActivity();
                }
            } );
            // negative for the right side
            builder.setNegativeButton( R.string.win_dialog_next_button,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    // start the next level
                    Game game = gameState.getGame();
                    startLevel( game.getLevel() + 1 );
                }
            } );
        } else if ( id == DIALOG_GOAL_MOLECULE ) {
            builder.setTitle( "Goal" ); // placeholder to be populated later
            
            // inflate the layout
            LayoutInflater inflater = ( LayoutInflater )
                    super.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View layout = inflater.inflate( R.layout.goal_dialog,
                    ( ViewGroup )super.findViewById( R.id.goal_dialog ) );
            LinearLayout l = ( LinearLayout )layout;
            
            // add the goal view
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT );
            params.gravity = Gravity.CENTER_HORIZONTAL;
            l.addView( view.getGoalView(), params );
            
            builder.setView( layout );
            
            // set the "Return to Game" button
            builder.setPositiveButton( R.string.goal_dialog_return_button,
                    new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int id ) {
                    dialog.dismiss();
                }
            } );
        }
        
        return builder.create();
    }
    
    /**
     * Called to prepare the specified dialog for viewing.
     * 
     * @param   id      the ID of the dialog to be prepared
     * @param   dialog  the actual <tt>Dialog</tt> to be prepared
     */
    @Override
    protected void onPrepareDialog( int id, Dialog dialog ) {
        super.onPrepareDialog( id, dialog );
        Resources resources = super.getResources();
        Game game = gameState.getGame();

        int level = game.getLevel();
        int seconds = game.getSeconds();
        int moves = game.getMoves();
        
        if ( id == DIALOG_WIN_LEVEL ) {
            AlertDialog d = ( AlertDialog )dialog;
            
            String fmt = resources.getString( R.string.win_dialog_text );
            String text = String.format( fmt, level, seconds, moves );
            d.setMessage( text );
        } else if ( id == DIALOG_GOAL_MOLECULE ) {
            AlertDialog d = ( AlertDialog )dialog;
            
            String fmt = resources.getString( R.string.goal_dialog_title );
            String text = String.format( fmt, level );
            d.setTitle( text );
        }
    }
    
    // ===== Assorted Atomix Methods =====
    
    /**
     * Starts the Level List Activity which may return a request to start a new
     * level.
     */
    private void startLevelListActivity() {
        Intent intent = new Intent( this, LevelListActivity.class );
        Bundle extras = new Bundle();
        extras.putSerializable( GameState.GAME_STATE_KEY, gameState );
        intent.putExtras( extras );
        
        // run the activity!
        super.startActivityForResult( intent, LEVEL_LIST_REQUEST_CODE );
    }
    
    /**
     * Redraws the activity's view.
     * <p>
     * The specified <tt>Rect</tt> object is used as the cropped area to redraw.
     * This argument may be <tt>null</tt> to redraw the entire view.
     * @param rect
     */
    public void redrawView( Rect rect ) {
        Message msg = new Message();
        msg.what = EVENT_REDRAW_VIEW;
        msg.obj = rect;
        viewHandler.sendMessage( msg );
    }
    
    /**
     * Invokes all events to win the current game and prompt the user to
     * continue to the next level.
     */
    public void winLevel() {
        // stop the old game timer
        GameController.stopTimer( gameState );
        
        // save the old game as finished
        Game game = gameState.getGame();
        db.update( game );
        
        // show the dialog and then start the next level
        viewHandler.sendEmptyMessage( EVENT_WIN_LEVEL );
    }
    
    /**
     * This method performs user intention confirmation checks and potentially
     * starts the specified level.
     * <p>
     * First, this checks to see if the current game is completed, and if not,
     * prompts the user to confirm their intention to abandon the unsaved game.
     * If the user agrees, the <tt>checkOverWriteOldLevel()</tt> method is
     * called.
     * 
     * @param   level   the level number of the new level
     */
    private void confirmAndStartLevel( int level ) {
        pendingLevel = level;
        
        // is this game finished?  if not, confirm the unsaved lost game
        if ( ! gameState.isFinished() ) {
            showDialog( DIALOG_CONFIRM_UNSAVED_LEVEL );
        } else {
            // check for level overwrite
            checkOverwriteOldLevel();
        }
    }
    
    /**
     * This method checks to see if starting the currently pending level will
     * overwrite a saved game, and if so prompts the user to confirm their
     * intention to overwrite it.
     */
    private void checkOverwriteOldLevel() {
        // is the new level already completed?
        if ( db.isLevelCompleted( gameState.getUser(), pendingLevel ) ) {
            // the game is finished, no unsaved dialog needed
            // instead, check for an overwrite of the chosen new level
            showDialog( DIALOG_CONFIRM_OVERWRITE_LEVEL );
        } else {
            // start the level
            startLevel( pendingLevel );
        }
    }
    
    /**
     * This method starts the specified level.  If the curent game is not
     * finished, it is deleted.  If the level to be started has already been
     * completed, it is deleted.  The game timer is started by this method.
     * <tt>
     * This starts the level for the current user by creating a new game at the
     * specified level and updating the game state and view.  This method does
     * not perform any persistence for the user's current game (this should be
     * handled prior to calling this method).
     * 
     * @param   level   the level to be started
     */
    private void startLevel( int level ) {
        User user = gameState.getUser();
        Game game = gameState.getGame();
        
        // delete and terminate the existing game (if it's not finished)
        if ( ! game.isFinished() ) {
            db.deleteGame( game.getId() );
        }
        
        // delete the existing game at this level (if there is one)
        if ( db.isLevelCompleted( user, level ) ) {
            db.deleteSavedGame( user, level );
        }
        
        // setup the new game
        Game newLevel = GameController.newLevel( user, level );
        user.setCurrentGame( newLevel );
        db.insert( newLevel );
        db.update( user );
        
        // set the new game state
        gameState = new GameState( user, newLevel );
        view.setGameState( gameState );
        
        // start the playing timer
        GameController.startTimer( gameState );
        
        // update the view with the new game
        redrawView( null );
    }
    
} // AtomicActivity

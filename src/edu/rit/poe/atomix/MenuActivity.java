/*
 * MenuActivity.java
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import edu.rit.poe.atomix.db.AtomixDbAdapter;
import edu.rit.poe.atomix.db.Game;
import edu.rit.poe.atomix.db.User;
import edu.rit.poe.atomix.game.GameController;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.LevelManager;

/**
 * The Main Menu activity of the Atomix game.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class MenuActivity extends Activity {
    
    public static final String LOG_TAG = "MENU_ACTIVITY";
    
    /** The subactivity request code. */
    public static final int GAME_REQUEST_CODE = 0x0;
    
    /** The subactivity result code for quitting the application. */
    public static final int GAME_RESULT_QUIT = 0x1;
    
    /** The subactivity result code for returning to the main menu. */
    public static final int GAME_RESULT_MAIN_MENU = 0x42;
    
    private static final int NEW_USER_DIALOG = 0x0;
    
    private static final int CONTINUE_DIALOG = 0x1;
    
    private static final int CONTEXT_MENU_DELETE = 0x0;
    
    /** The activity's database adapter. */
    private AtomixDbAdapter db;
    
    /** The error message for a blank name in the new game dialog. */
    private Toast blankNameMessage;
    
    private ListAdapter continueListAdapter;
    
    /**
     * Called when the activity is first created.  This method initializes home
     * screen buttons and any background state that needs to be initialized,
     * depending on the current state.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Log.d( LOG_TAG, "onCreate()" );
        
        //super.setRequestedOrientation(
        //        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        
        // remove the titlebar (it's not needed)
        super.requestWindowFeature( Window.FEATURE_NO_TITLE );
        
        // set the main menu content view
        super.setContentView( R.layout.menu );
        
        // set the gradient background
        GradientDrawable grad = new GradientDrawable( Orientation.TOP_BOTTOM,
            new int[] { Color.BLACK, Color.parseColor( "#CC0000" ) } );
        grad.setGradientType( GradientDrawable.LINEAR_GRADIENT );
        super.getWindow().setBackgroundDrawable( grad );
        
        // initialize the level manager
        LevelManager lm = LevelManager.getInstance();
        lm.init( this );
        
        // load the game database
        db = new AtomixDbAdapter( this );
        db.open();
        
        // load all saved users/games
        final Cursor savedUsers = db.getSavedUsers();
        super.startManagingCursor( savedUsers );
        
        Log.d( LOG_TAG, "Saved games: " + savedUsers.getCount() );
        
        // setup the Continue button
        LinearLayout menu =
                ( LinearLayout )findViewById( R.id.main_button_list );
        Button resume = ( Button )findViewById( R.id.continue_button );
        if ( savedUsers.getCount() > 0 ) {
            Log.d( LOG_TAG, "Display CONTINUE button." );
            
            // setup button callbacks
            resume.setOnClickListener( new View.OnClickListener() {
                public void onClick( View view ) {
                    
                    // if there's only one game, start it!
                    if ( savedUsers.getCount() == 1 ) {
                        // start the one previously saved game
                        
                        // get the only user id
                        savedUsers.moveToFirst();
                        long userId = savedUsers.getLong(
                                savedUsers.getColumnIndex( "_id" ) );
                        
                        // query the selected user and load the saved game state
                        User user = db.getUser( userId );
                        
                        startGame( user );
                    } else {
                        // show the continue game dialog to choose which game
                        MenuActivity.this.showDialog( CONTINUE_DIALOG );
                    }
                }
            } );
            
        } else {
            menu.removeView( resume );
        }
        
        // setup the new game button
        Button newGame = ( Button )findViewById( R.id.new_game_button );
        // create the dialog to enter a name for a new game
        newGame.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                // show the new name dialog
                MenuActivity.this.showDialog( NEW_USER_DIALOG );
            }
        } );
        
        // set help functionality
        Button help = ( Button )findViewById( R.id.help_button );
        help.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                Toast toast = Toast.makeText( MenuActivity.this, "HELP!",
                        Toast.LENGTH_LONG );
                toast.show();
            }
        } );
        
        // set quit functionality
        Button quit = ( Button )findViewById( R.id.quit_button );
        quit.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                // just exit -- no reason to save here
                MenuActivity.super.finish();
            }
        } );
        
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Log.d( LOG_TAG, "onStart()" );
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d( LOG_TAG, "onResume()" );
        
        // connect to the database here
        if ( db == null ) {
            db = new AtomixDbAdapter( this );
            db.open();
        }
    }
    
     @Override
    public void onPause() {
        super.onPause();
        Log.d( LOG_TAG, "onPause()" );
        
        // close the connection to the database
        db.close();
        db = null;
        
        try {
            this.dismissDialog( NEW_USER_DIALOG );
        } catch ( Exception e ) {
            // ignore!
        }
        
        try {
            this.dismissDialog( CONTINUE_DIALOG );
        } catch ( Exception e ) {
            // ignore!
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        Log.d( LOG_TAG, "onStop()" );
    }
    
    @Override
    public void onRestart() {
        super.onRestart();
        Log.d( LOG_TAG, "onRestart()" );
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d( LOG_TAG, "DroidAtomix is now closing." );
    }
    
    /**
     * Starts the DroidAtomix game!
     * 
     * @param   user    the user that this game will be run under
     */
    private void startGame( User user ) {
        // create the intent for the game
        GameState gameState = new GameState( user, user.getCurrentGame() );
        Intent intent = new Intent( this, AtomicActivity.class );
        Bundle extras = new Bundle();
        extras.putSerializable( "game_state", gameState );
        intent.putExtras( extras );
        
        // start the game, and wait for a result
        super.startActivityForResult( intent, GAME_REQUEST_CODE );
    }
    
    @Override
    protected void onActivityResult( int requestCode, int resultCode,
            Intent data ) {
        if ( requestCode == GAME_REQUEST_CODE ) {
            if ( resultCode == GAME_RESULT_MAIN_MENU ) {
                // keep the main menu activity running
                
            } else {
                // end everything!
                MenuActivity.super.finish();
            }
        }
    }
    
    // ==== Dialog Methods ====
    
    @Override
    protected Dialog onCreateDialog( int id ) {
        Dialog dialog = null;
        
        LayoutInflater layoutInflater = LayoutInflater.from( this );
        if ( id == NEW_USER_DIALOG ) {
            // CREATE THE NEW USER DIALOG
            
            blankNameMessage = Toast.makeText( this, "Name cannot be blank",
                    Toast.LENGTH_LONG );
            
            final View view =
                    layoutInflater.inflate( R.layout.game_dialog, null );
            
            AlertDialog.Builder ad = new AlertDialog.Builder( this );
            ad.setTitle( "Start A New Game" );
            ad.setView( view );
            //ad.setIcon( android.R.drawable.ic_menu_more );
            
            // set the start game button
            Button startGame = ( Button )view.findViewById( R.id.play_button );
            startGame.setOnClickListener( new View.OnClickListener() {
                public void onClick( View v ) {
                    // check to ensure that the name isn't blank
                    EditText newName =
                            ( EditText )view.findViewById( R.id.new_name );
                    if ( newName.getText().toString().equals( "" ) ) {
                        blankNameMessage.show();
                    } else {
                        // create a new user
                        String username = newName.getText().toString();
                        User user = GameController.newUser( username );
                        
                        // create a new game and set it to active
                        Game game = GameController.newLevel( user,
                                LevelManager.FIRST_LEVEL );
                        user.setCurrentGame( game );
                        
                        // save the game for the first time
                        db.insert( user );
                        db.insert( game );
                        db.update( user );
                        
                        // start playing!
                        startGame( user );
                    }
                }
            } );
            
            dialog = ad.create();
            
        } else if ( id == CONTINUE_DIALOG ) {
            // CREATE THE CONTINUE GAME DIALOG
            View view =
                    layoutInflater.inflate( R.layout.continue_dialog, null );
            
            AlertDialog.Builder ad = new AlertDialog.Builder( this );
            ad.setTitle( "Continue a Saved Game" );
            ad.setView( view );
            ad.setIcon( android.R.drawable.ic_menu_more );
            
            ListView savedList =
                    ( ListView )view.findViewById( R.id.saved_games_list );
            savedList.setOnCreateContextMenuListener(
                    new View.OnCreateContextMenuListener() {
                public void onCreateContextMenu( ContextMenu menu, View view,
                        ContextMenuInfo menuInfo ) {
                    menu.setHeaderTitle( "Edit Saved Game" );
                    
                    MenuItem item = menu.add( Menu.NONE, CONTEXT_MENU_DELETE,
                            Menu.NONE, "Delete this user" );
                    
                    item.setOnMenuItemClickListener(
                            new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick( MenuItem item ) {
                            Log.d( LOG_TAG, "CLICKED" );
                            AdapterContextMenuInfo info =
                                    ( AdapterContextMenuInfo )
                                    item.getMenuInfo();
                            long id = continueListAdapter.getItemId(
                                    info.position );
                            
                            db.deleteUser( id );
                            
                            Toast toast = Toast.makeText( MenuActivity.this,
                                    "User deleted",
                                    Toast.LENGTH_LONG );
                            toast.show();
                            
                            MenuActivity.this.showDialog( CONTINUE_DIALOG );
                            
                            return true;
                        }
                    } );
                    
                    //@todo delete game?
                }
            } );
            savedList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                public void onItemClick( AdapterView<?> parent, View view,
                        int position, long id ) {
                    
                    // query the selected user and load the saved game state
                    User user = db.getUser( id );
                    
                    // start the selected game!
                    startGame( user );
                }
            } );
            
            dialog = ad.create();
        } else {
            // nothing?
        }
        
        return dialog;
    }
    
    @Override
    protected void onPrepareDialog( int id, Dialog dialog ) {
        super.onPrepareDialog( id, dialog );
        
        if ( id == NEW_USER_DIALOG ) {
            // clear the new name field
            EditText newName = ( EditText )dialog.findViewById( R.id.new_name );
            newName.setText( "" );
        
        } else if ( id == CONTINUE_DIALOG ) {
            String[] from = new String[]{ User.USERNAME_KEY, Game.SAVED_KEY };
            int[] to = new int[]{ android.R.id.text1, android.R.id.text2 };
            
            ListView savedList =
                    ( ListView )dialog.findViewById( R.id.saved_games_list );
            
            Cursor savedUsers = db.getSavedUsers();
            super.startManagingCursor( savedUsers );
            continueListAdapter = new SimpleCursorAdapter( this,
                    android.R.layout.simple_list_item_2, savedUsers, from, to );
            savedList.setAdapter( continueListAdapter );
        }
    }
    
} // MenuActivity

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
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class MenuActivity extends Activity {
    
    /**  */
    private AtomixDbAdapter db;
    
    /**  */
    private AlertDialog newGameDialog;
    
    private AlertDialog continueGameDialog;
    
    private EditText newName;
    
    private Toast blankNameMessage;
    
    /**
     * Called when the activity is first created.  This method initializes home
     * screen buttons and any background state that needs to be initialized,
     * depending on the current state.
     * 
     * @param   icicle  the bundle of saved state information
     */
    @Override
    public void onCreate( final Bundle icicle ) {
        super.onCreate( icicle );
        
        Log.d( "MenuActivity", "onCreate() called." );
        
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
        try {
            db = new AtomixDbAdapter( this );
            db.open();
        } catch ( SQLException e ) {
            Log.e( "DROID_ATOMIX", "Error loading game database: "
                    + Log.getStackTraceString( e ) );
        }
        
        // load all saved users/games
        final Cursor saved = db.getSavedUsers();
        super.startManagingCursor( saved );
        
        Log.d( "DROID_ATOMIX", "Saved games: " + saved.getCount() );
        
        // setup the Continue button
        LinearLayout menu =
                ( LinearLayout )findViewById( R.id.main_button_list );
        Button resume = ( Button )findViewById( R.id.continue_button );
        if ( saved.getCount() > 0 ) {
            Log.d( "DROID_ATOMIX", "Display CONTINUE button." );
            
            // create the dialog that lists games to be continued
            if ( continueGameDialog == null ) {
                createContinueDialog( saved );
            }
            
            // setup button callbacks
            resume.setOnClickListener( new View.OnClickListener() {
                public void onClick( View view ) {

                    // if there's only one game, start it!
                    if ( saved.getCount() == 1 ) {
                        // start the one previously saved game
                        
                        // get the only user id
                        saved.moveToFirst();
                        long userId = saved.getLong(
                                saved.getColumnIndex( "_id" ) );
                        
                        // query the selected user and load the saved game state
                        User user = db.getUser( userId );
                        
                        startGame( user, user.getSavedGame() );
                    } else {
                        // show the continue game dialog to choose which game
                        continueGameDialog.show();
                    }
                }
            } );
            
        } else {
            menu.removeView( resume );
        }
        
        // setup the new game button
        Button newGame = ( Button )findViewById( R.id.new_game_button );
        // create the dialog to enter a name for a new game
        if ( newGameDialog == null ) {
            createNewGameDialog();
        }
        newGame.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                // show the new name dialog
                if ( newName != null ) {
                    newName.setText( "" );
                }
                newGameDialog.show();
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
    
    /**
     * Starts the DroidAtomix game!
     */
    private void startGame( User user, Game game ) {
        // create the intent for the game
        GameState gameState = new GameState( user, game );
        Intent intent = new Intent( this, AtomicActivity.class );
        Bundle extras = new Bundle();
        extras.putSerializable( "game_state", gameState );
        intent.putExtras( extras );
        
        // start the game!
        super.startActivity( intent );
        
        // do not return to the home screen
        MenuActivity.super.finish();
    }
    
    private void createNewGameDialog() {
        blankNameMessage = Toast.makeText( this, "Name cannot be blank",
                Toast.LENGTH_LONG );
        
        LayoutInflater layoutInflator = LayoutInflater.from( this );
        View view = layoutInflator.inflate( R.layout.game_dialog, null );
        
        AlertDialog.Builder ad = new AlertDialog.Builder( this );
        ad.setTitle( "Start A New Game" );
        ad.setView( view );
        ad.setIcon( android.R.drawable.ic_menu_more );
        
        newName = ( EditText )view.findViewById( R.id.new_name );
        
        // set the start game button
        Button startGame = ( Button )view.findViewById( R.id.play_button );
        startGame.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                // check to ensure that the name isn't blank
                if ( newName.getText().toString().equals( "" ) ) {
                    blankNameMessage.show();
                } else {
                    // create a new user
                    String username = newName.getText().toString();
                    User user = GameController.newUser( username );
                    
                    // create a new game and set it to active
                    Game game = GameController.newLevel( user,
                            LevelManager.FIRST_LEVEL );
                    user.setSavedGame( game );
                    
                    // save the game for the first time
                    db.insert( user );
                    db.insert( game );
                    db.update( user );
                    
                    // start playing!
                    startGame( user, game );
                }
            }
        } );
        
        newGameDialog = ad.create();
    }
    
    private void createContinueDialog( Cursor savedUsers ) {
        final LayoutInflater layoutInflater = LayoutInflater.from( this );
        View view = layoutInflater.inflate( R.layout.continue_dialog, null );
        
        AlertDialog.Builder ad = new AlertDialog.Builder( this );
        ad.setTitle( "Continue a Saved Game" );
        ad.setView( view );
        ad.setIcon( android.R.drawable.ic_menu_more );
        
        ListView savedList =
                ( ListView )view.findViewById( R.id.saved_games_list );
        String[] from = new String[]{ User.USERNAME_KEY, Game.SAVED_KEY };
        int[] to = new int[]{ android.R.id.text1, android.R.id.text2 };
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter( this,
                android.R.layout.simple_list_item_2, savedUsers, from, to );
        savedList.setAdapter( adapter );
        
        savedList.setOnCreateContextMenuListener(
                new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu( ContextMenu menu, View view,
                    ContextMenuInfo menuInfo ) {
                menu.setHeaderTitle( "Edit Saved Game" );
                
                //@todo delete game?
            }
        } );
        
        savedList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            public void onItemClick( AdapterView<?> parent, View view,
                    int position, long id ) {
                
                // query the selected user and load the saved game state
                User user = db.getUser( id );
                
                // close this dialog
                continueGameDialog.dismiss();
                
                // start the selected game!
                startGame( user, user.getSavedGame() );
            }
        } );
        
        continueGameDialog = ad.create();
    }
    
    @Override
    protected void onSaveInstanceState( Bundle icicle ) {
        super.onSaveInstanceState( icicle );
        Log.d( "DROID_ATOMIX", "onSaveInstanceState() called." );
        
        Bundle dialogBundle = newGameDialog.onSaveInstanceState();
        icicle.putBundle( "dialog-bundle", dialogBundle );
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d( "DROID_ATOMIX", "onPause() called." );
        
        // close the connection to the database
        db.close();
        
        if ( newGameDialog.isShowing() ) {
            newGameDialog.dismiss();
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        // close the connection to the database
        db.close();
        
        if ( newGameDialog.isShowing() ) {
            newGameDialog.dismiss();
        }
    }
    
    @Override
    protected void onRestoreInstanceState( Bundle icicle ) {
        super.onRestoreInstanceState( icicle );
        Log.d( "DROID_ATOMIX", "onRestoreInstanceState() called." );
        
        Bundle dialogBundle = icicle.getBundle( "dialog-bundle" );
        if ( dialogBundle != null ) {
            newGameDialog.onRestoreInstanceState( dialogBundle );
        }
    }
    
} // MenuActivity

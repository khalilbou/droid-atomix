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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import edu.rit.poe.atomix.game.GameDatabase;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.LevelManager;
import java.util.List;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class MenuActivity extends Activity {
    
    /**  */
    private AlertDialog newGameDialog;
    
    private EditText newName;
    
    private Toast blankNameMessage;
    
    /**
     * Called when the activity is first created.
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
        
        super.setContentView( R.layout.menu );
        
        GradientDrawable grad = new GradientDrawable( Orientation.TOP_BOTTOM,
            new int[] { Color.BLACK, Color.parseColor( "#CC0000" ) } );
        grad.setGradientRadius( 100.0f );
        grad.setGradientType( GradientDrawable.LINEAR_GRADIENT );
        
        super.getWindow().setBackgroundDrawable( grad );
        
        // initialize the level manager
        LevelManager lm = LevelManager.getInstance();
        lm.init( this );
        
        // load the game database
        try {
            // don't load database twice
            if ( ! GameDatabase.isLoaded() ) {
                GameDatabase.load( this );
            }
        } catch ( Exception e ) {
            // @todo ignore for now
        }
        
        // set the "Continue" button functionality, if any
        final List<GameState> states = GameDatabase.getActive();
        
        Log.d( "DROID_ATOMIX", "SAVED ACTIVE GAMES:" );
        for ( GameState state : states ) {
            Log.d( "DROID_ATOMIX", "GAME: " + state.getUser() );
        }
        
        LinearLayout menu =
                ( LinearLayout )findViewById( R.id.main_button_list );
        Button resume = ( Button )findViewById( R.id.continue_button );
        if ( ! states.isEmpty() ) {
            // setup button callbacks
            resume.setOnClickListener( new View.OnClickListener() {
                public void onClick( View view ) {
                    
                    // if there's only one game, start it!
                    if ( states.size() == 1 ) {
                        // set the one game
                        GameState.setCurrent( GameDatabase.getLast() );
                    } else {
                        
                        // display a pop-up to select which game
                        
                        ListView list = new ListView( MenuActivity.this );
            
                        AlertDialog.Builder ad =
                                new AlertDialog.Builder( MenuActivity.this );
                        ad.setTitle( "Continue A Saved Game" );
                        ad.setView( list );
                        ad.show();
                        
                    }
                    
                    // start the game!
                    Intent intent = new Intent( MenuActivity.this,
                            AtomicActivity.class );
                    MenuActivity.super.startActivity( intent );
                    
                    // for now, allow returning to the menu screen
                    //MenuActivity.super.finish();
                }
            } );
        } else {
            menu.removeView( resume );
        }
        
        // set the new game button
        if ( newGameDialog == null ) {
            blankNameMessage = Toast.makeText( this, "Name cannot be blank",
                    Toast.LENGTH_LONG );
            
            LayoutInflater li = LayoutInflater.from( this );
            View view = li.inflate( R.layout.game_dialog, null );
            
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
                        // create a new game
                        GameState.setCurrent( GameDatabase.newGame(
                                newName.getText().toString() ) );
                        
                        Intent i = new Intent( MenuActivity.this,
                                AtomicActivity.class );
                        MenuActivity.this.startActivity( i );

                        // do not return to the menu activity with the back
                        // button
                        MenuActivity.this.finish();
                    }
                }
            } );
            
            newGameDialog = ad.create();
        }
        
        Button newGame = ( Button )findViewById( R.id.new_game_button );
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
                MenuActivity.super.finish();
            }
        } );
        
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

/*
 * LevelListActivity.java
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

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import edu.rit.poe.atomix.db.AtomixDbAdapter;
import edu.rit.poe.atomix.db.Game;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.LevelManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class LevelListActivity extends ListActivity {
    
    private Resources resources;
    
    private AtomixDbAdapter db;
    
    private Cursor levelCursor;
    
    /**
     * Called when the activity is first created.
     *
     * @param   icicle  the bundle of saved data
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        
        // access the application resources
        resources = super.getResources();
        
        super.setTitle( resources.getString( R.string.levels_title) );
        
        // get the game state from the intent's extras
        Intent intent = super.getIntent();
        Bundle extras = intent.getExtras();
        GameState gameState =
                ( GameState )extras.getSerializable( GameState.GAME_STATE_KEY );
        
        // load the current users games in a cursor
        db = new AtomixDbAdapter( this ).open();
        
        final Cursor games = db.getFinishedGames( gameState.getUser().getId() );
        super.startManagingCursor( games );
        
        // load all finished levels' time into a map
        final Map<Integer, Integer> finished = new HashMap<Integer, Integer>();
        
        for ( int i = 0; i < games.getCount(); i++ ) {
            games.moveToNext();
            
            int level = games.getInt( games.getColumnIndex( Game.LEVEL_KEY ) );
            finished.put( level, games.getPosition() );
        }
        
        LevelManager levelManager = LevelManager.getInstance();
        levelCursor = levelManager.getLevels();
        
        String[] from = new String[]{ Game.LEVEL_KEY, "name" };
        int[] to = new int[]{ R.id.level_number, R.id.level_name };
        
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter( this,
                R.layout.level_row, levelCursor, from, to ) {
            
            @Override
            public View getView( int pos, View convertView, ViewGroup parent ) {
                View view = super.getView( pos, convertView, parent );
                
                TextView secondsText = ( TextView )view.findViewById(
                        R.id.level_completed_seconds );
                TextView movesText = ( TextView )view.findViewById(
                        R.id.level_completed_moves );
                
                // find out what level this is
                levelCursor.moveToPosition( pos );
                int level = levelCursor.getInt(
                        levelCursor.getColumnIndex( "_id" ) );
                
                if ( finished.containsKey( level ) ) {
                    // the value in the map at this level is the position in the
                    // games cursor
                    int gamesPos = finished.get( level );
                    games.moveToPosition( gamesPos );
                    
                    int seconds = games.getInt(
                            games.getColumnIndex( Game.SECONDS_KEY ) );
                    String fmt = resources.getString(
                            R.string.completed_seconds_text );
                    String text = String.format( fmt, seconds );
                    secondsText.setText( text );
                    
                    int moves = games.getInt(
                            games.getColumnIndex( Game.MOVES_KEY ) );
                    fmt = resources.getString( R.string.completed_moves_text );
                    text = String.format( fmt, moves );
                    movesText.setText( text );
                }
                
                return view;
            }
        };
        
        this.setListAdapter( adapter );
    }
    
    @Override
    public void onListItemClick( ListView lv, View v, int position, long id ) {
        // end this activity and tell the parent to start the specified level
        int level = ( int )id;
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putSerializable( Game.LEVEL_KEY, level );
        intent.putExtras( extras );
        super.setResult( AtomicActivity.START_NEW_LEVEL_RESULT_CODE, intent );
        super.finish();
    }
    
    @Override
    protected Dialog onCreateDialog( int id ) {
        
        return null;
    }
    
    @Override
    protected void onPrepareDialog( int id, Dialog dialog ) {
        super.onPrepareDialog( id, dialog );
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // close the connection to the database
        db.close();
        db = null;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // turn the database adapter back on
        if ( db == null ) {
            db = new AtomixDbAdapter( this ).open();
        }
    }
    
} // LevelListActivity

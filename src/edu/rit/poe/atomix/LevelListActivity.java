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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import edu.rit.poe.atomix.game.GameState;
import edu.rit.poe.atomix.levels.LevelManager;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class LevelListActivity extends ListActivity {
    
    private Cursor levelCursor;
    
    static int i = 0;
    
    /**
     * Called when the activity is first created.
     *
     * @param   icicle  the bundle of saved data
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        
        super.setTitle( "DroidAtomix Levels" );
        
        // get the game state from the intent's extras
        Intent intent = super.getIntent();
        Bundle extras = intent.getExtras();
        GameState gameState =
                ( GameState )extras.getSerializable( GameState.GAME_STATE_KEY );
        
        LevelManager levelManager = LevelManager.getInstance();
        levelCursor = levelManager.getLevels();
        
        String[] from = new String[]{ "level", "name" };
        int[] to = new int[]{ R.id.level_number, R.id.level_name };
        
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter( this,
                R.layout.level_row, levelCursor, from, to ) {
            
            @Override
            public View getView( int pos, View convertView, ViewGroup parent ) {
                View view = super.getView( pos, convertView, parent );
                
                TextView levelCompleted =
                        ( TextView )view.findViewById( R.id.level_completed );
                
                if ( ( ( i++ ) % 2 ) == 0 ) {
                    levelCompleted.setText( "Completed" );
                }
                
                return view;
            }
        };
        
        this.setListAdapter( adapter );
    }
    
    @Override
    public void onListItemClick( ListView lv, View v, int position, long id ) {
        AtomicActivity atomix = ( AtomicActivity )super.getParent();
        
        atomix.winLevel();
        
        
        // @todo COMPLETE
        
        // 1. confirm with a dialog box
        
        // 2. then kill this process and set the game state of the existing
        //    AtomicActivity (make sure this is fluid)
        
    }
    
    @Override
    protected Dialog onCreateDialog( int id ) {
        
        return null;
    }
    
    @Override
    protected void onPrepareDialog( int id, Dialog dialog ) {
        super.onPrepareDialog( id, dialog );
        
    }
    
} // LevelListActivity

/*
 * LevelListActivity.java
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

import android.app.Dialog;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
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

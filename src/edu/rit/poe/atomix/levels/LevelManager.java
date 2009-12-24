/*
 * LevelManager.java
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

package edu.rit.poe.atomix.levels;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class LevelManager {
    
    public static final int FIRST_LEVEL = 1;
    
    private static volatile LevelManager instance;
    
    private Map<Integer, Level> levelMap;
    
    /**
     * Constructs a new <tt>LevelManager</tt>.
     */
    private LevelManager() {
    }
    
    public static final LevelManager getInstance() {
        // DCL anti-pattern avoided by way of 'volatile'
        if ( instance == null ) {
            synchronized( LevelManager.class ) {
                if ( instance == null ) {
                    instance = new LevelManager();
                }
            }
        }
        return instance;
    }
    
    public void init( Context context ) {
        levelMap = new HashMap<Integer, Level>();
        
        Level level = null;
        try {
            AssetManager am = context.getAssets();
            
            // get all level files in the "levels" directory
            String[] levelFiles = am.list( "levels" );
            Arrays.sort( levelFiles );
            
            Log.d( "LevelManager", "Level File List:" );
            
            for ( String levelFile : levelFiles ) {
                Log.d( "LevelManager FILE:", levelFile );
                
                InputStream is = am.open( "levels/" + levelFile );
                level = Level.loadLevel( is );
                
                levelMap.put( level.getLevel(), level );
            }
        } catch ( Exception e ) {
            // ignore
        }
    }
    
    public Level getLevel( int levelNumber ) throws IllegalArgumentException {
        Level level = null;
        if ( ( level = levelMap.get( levelNumber ) ) == null ) {
            throw new IllegalArgumentException(
                    "Level " + levelNumber + " does not exist." );
        }
        return level;
    }
    
    public Cursor getLevels() {
        MatrixCursor cursor =
                new MatrixCursor( new String[] { "_id", "level", "name" } );
        
        for ( Map.Entry<Integer, Level> entry : levelMap.entrySet() ) {
            int level = entry.getKey();
            
            Object[] row = new Object[ 3 ];
            row[ 0 ] = level;
            row[ 1 ] = "Level " + level;
            row[ 2 ] = entry.getValue().getName();
            
            cursor.addRow( row );
        }
        
        return cursor;
    }
    
} // LevelManager

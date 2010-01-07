/*
 * LevelManager.java
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
    
    public boolean hasLevel( int levelNumber ) {
        return levelMap.containsKey( levelNumber );
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

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
import android.util.Log;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to manage and store all levels that are read from the Android assets.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class LevelManager {
    
    /** A constant for the first level. */
    public static final int FIRST_LEVEL = 1;
    
    /** A constant for the levels directory where level files are stored. */
    public static final String LEVELS_DIRECTORY = "levels";
    
    /** The singleton instance of this class. */
    private static volatile LevelManager instance;
    
    /** The map of level objects, mapped by their respective level numbers. */
    private Map<Integer, Level> levelMap;
    
    /**
     * Constructs a new <tt>LevelManager</tt>.
     */
    private LevelManager() {
    }
    
    /**
     * Returns the singleton instance of this class.
     * 
     * @return  the singleton instance of this class
     */
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
    
    /**
     * Initializes the levels stored in this object.
     * 
     * @param   context     the application context
     */
    public void init( Context context ) {
        levelMap = new HashMap<Integer, Level>();
        
        Level level = null;
        try {
            AssetManager am = context.getAssets();
            
            // get all level files in the "levels" directory
            String[] levelFiles = am.list( LEVELS_DIRECTORY );
            Log.d( "LevelManager", "Levels: " + levelFiles.length );
            Log.d( "LevelManager", "Level File List:" );
            for ( String levelFile : levelFiles ) {
                Log.d( "LevelManager FILE:", levelFile );
                
                InputStream is = am.open( LEVELS_DIRECTORY + File.separator
                        + levelFile );
                level = Level.loadLevel( is );
                
                levelMap.put( level.getLevel(), level );
            }
        } catch ( Exception e ) {
            Log.e( "LevelManager", Log.getStackTraceString( e ) );
        }
    }
    
    /**
     * Returns the level with the specified level number.
     * 
     * @param   levelNumber     the level number to return a level for
     * 
     * @return                  the level identified by the specified level
     *                          number, or <tt>null</tt> if no such level exists
     */
    public Level getLevel( int levelNumber ) {
        return levelMap.get( levelNumber );
    }
    
    /**
     * Returns whether the specified level exists.
     * 
     * @param   levelNumber     the level numebr of the level to check
     * 
     * @return                  <tt>true</tt> if the level exists, otherwise
     *                          <tt>false</tt>
     */
    public boolean hasLevel( int levelNumber ) {
        return levelMap.containsKey( levelNumber );
    }
    
    /**
     * Returns a list of all available levels, sorted in ascending order.
     * 
     * @return  a list of all levels
     */
    public List<Level> getLevels() {
        List<Level> list = new ArrayList<Level>( levelMap.values() );
        Collections.sort( list );
        return list;
    }
    
} // LevelManager

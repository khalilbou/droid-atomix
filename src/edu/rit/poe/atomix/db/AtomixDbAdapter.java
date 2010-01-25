/*
 * AtomixDbAdapter.java
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

package edu.rit.poe.atomix.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import edu.rit.poe.atomix.R;
import edu.rit.poe.atomix.util.Point;
import java.util.Calendar;
import java.util.Map;

/**
 * The Atomix database adapter.
 * 
 * @author  Peter O. Erickson
 * 
 * @version $Id$
 */
public class AtomixDbAdapter {
    
    /** The current database version. */
    private static final int DATABASE_VERSION = 1;
    
    /** The name of the application's SQLite database. */
    public static final String DATABASE_NAME = "Atomix";
    
    /** The canonical name of the <tt>USER</tt> table. */
    public static final String USER_TABLE_NAME = "USER";
    
    /** The canonical name of the <tt>GAME</tt> table. */
    public static final String GAME_TABLE_NAME = "GAME";
    
    /** The canonical name of the <tt>ATOM</tt> table. */
    public static final String ATOM_TABLE_NAME = "ATOM";
    
    /** A constant used for cursor IDs. */
    public static final String ID = "_id";
    
    /** A constant for the number of milliseconds per second. */
    public static final int MS_PER_S = 1000;
    
    /** An object that represents a connection to the application's database. */
    private SQLiteDatabase database;
    
    /** A database helper for creating, upgrading, and accessing the database */
    private DatabaseHelper databaseHelper;
    
    /** The application's context. */
    private final Context context;
    
    /**
     * Creates a new database adapter.
     * 
     * @param   context     the <tt>Context</tt> within which to work
     */
    public AtomixDbAdapter( Context context ) {
        this.context = context;
    }
    
    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * 
     * @throws SQLException if the database could be neither opened or created
     */
    public AtomixDbAdapter open() throws SQLException {
        databaseHelper = new DatabaseHelper( context, DATABASE_NAME, null,
                DATABASE_VERSION );
        database = databaseHelper.getWritableDatabase();
        
        return this;
    }
    
    /**
     * Closes the connection the database.
     */
    public void close() {
        databaseHelper.close();
    }
    
    /**
     * Helper method to populate a new <tt>ContentValues</tt> object with the
     * data from the specified <tt>User</tt> object.
     * 
     * @param   user    the <tt>User</tt> object from which to obtain data
     * 
     * @return          a new <tt>ContentValues</tt> object with populated data
     */
    private ContentValues getValues( User user ) {
        ContentValues values = new ContentValues();
        values.put( User.USERNAME_KEY, user.getUsername() );
        
        // only save the game ID if it's valid
        Game game = user.getCurrentGame();
        if ( ( game != null ) && ( game.getId() != -1 ) ) {
            values.put( User.SAVED_GAME_ID_KEY, game.getId() );
        }
        
        return values;
    }
    
    /**
     * Inserts the specified <tt>User</tt> into the database.  This method will
     * set the user's new <tt>id</tt>.  The ID of the currently active game will
     * only be inserted if one exists.
     * <p>
     * This method will <b>not</b> cascade to insert the user's
     * <tt>savedGame</tt> object.
     * 
     * @param   user    the user to be persisted to the database
     */
    public void insert( User user ) {
        ContentValues values = getValues( user );
        
        // insert into the DB and set the user ID
        long userId = database.insert( USER_TABLE_NAME, null, values );
        user.setId( userId );
    }
    
    /**
     * Updates the specified <tt>User</tt> in the database. This method will
     * save all state of the specified object to the database row which matches
     * the user's <tt>id</tt>.
     * <p>
     * This method will <b>not</b> cascade to save the user's <tt>savedGame</tt>
     * object.
     * 
     * @param   user    the user to be updated in the database
     */
    public void update( User user ) {
        ContentValues values = getValues( user );
        
        String where = User.ID_KEY + "=" + user.getId();
        if ( database.update( USER_TABLE_NAME, values, where, null ) == 0 ) {
            throw new SQLException( "Error saving user: "
                    + user.getUsername() );
        }
    }
    
    /**
     * Helper method to populate a new <tt>ContentValues</tt> object with the
     * data from the specified <tt>Game</tt> object.
     * 
     * @param   game    the <tt>Game</tt> object from which to obtain data
     * 
     * @return          a new <tt>ContentValues</tt> object with populated data
     */
    private ContentValues getValues( Game game ) {
        ContentValues values = new ContentValues();
        values.put( Game.CREATED_KEY,
                ( game.getCreated().getTimeInMillis() / 1000L ) );
        values.put( Game.LEVEL_KEY, game.getLevel() );
        values.put( Game.MOVES_KEY, game.getMoves() );
        values.put( Game.SECONDS_KEY, game.getSeconds() );
        
        int finished = ( game.isFinished() ? 1 : 0 );
        values.put( Game.FINISHED_KEY, finished );
        
        // set right now as the saved time (an insert/update will happen now)
        Calendar now = Calendar.getInstance();
        values.put( Game.SAVED_KEY, ( now.getTimeInMillis() / 1000L ) );
        
        // only save the user ID if it's valid
        User user  = game.getUser();
        if ( ( user != null ) && ( user.getId() != -1 ) ) {
            values.put( Game.USER_ID_KEY, user.getId() );
        }
        
        return values;
    }
    
    /**
     * Inserts the specified <tt>Game</tt> into the database.  This method will
     * set the game's new <tt>id</tt>.
     * <p>
     * This method will <b>not</b> cascade to insert the game's user.
     * 
     * @param   game    the game to be persisted to the database
     */
    public void insert( Game game ) {
        ContentValues values = getValues( game );
        
        // insert into the DB and set the game ID
        long gameId = database.insert( GAME_TABLE_NAME, null, values );
        game.setId( gameId );
        
        // insert the atom locations also
        Map<Short, Point> atoms = game.getAtoms();
        for ( Map.Entry<Short, Point> atom : atoms.entrySet() ) {
            values.clear();
            // populate the values of each atom
            values.put( Game.ATOM_GAME_ID_KEY, game.getId() );
            values.put( Game.ATOM_MARKER_KEY, atom.getKey() );
            values.put( Game.ATOM_X_KEY, atom.getValue().x );
            values.put( Game.ATOM_Y_KEY, atom.getValue().y );
            
            // insert each atom, disregard the row ID
            database.insert( ATOM_TABLE_NAME, null, values );
        }
    }
    
    /**
     * Updates the specified <tt>Game</tt> in the database. This method will
     * save all state of the specified object to the database row which matches
     * the game's <tt>id</tt>.
     * <p>
     * This method will <b>not</b> cascade to save the game's user.
     * 
     * @param   game    the game to be updated in the database
     */
    public void update( Game game ) {
        ContentValues values = getValues( game );
        
        // update the main Game information
        String where = Game.ID_KEY + "=" + game.getId();
        if ( database.update( GAME_TABLE_NAME, values, where, null ) == 0 ) {
            throw new SQLException(
                    "Error saving game (ID: " + game.getId() + ")." );
        }
        
        // update atom locations in the game
        Map<Short, Point> atoms = game.getAtoms();
        for ( Map.Entry<Short, Point> atom : atoms.entrySet() ) {
            values.clear();
            // populate the values of each atom
            values.put( Game.ATOM_X_KEY, atom.getValue().x );
            values.put( Game.ATOM_Y_KEY, atom.getValue().y );
            
            where = Game.ATOM_GAME_ID_KEY + "=" + game.getId() + " AND "
                    + Game.ATOM_MARKER_KEY + "=" + atom.getKey();
            if ( database.update(
                    ATOM_TABLE_NAME, values, where, null ) == 0 ) {
                throw new SQLException( "Error saving atom." );
            }
        }
    }
    
    /**
     * Retrieves the user with the specified ID.  This method <b>will</b>
     * cascade to retrieve the user's current game.
     * 
     * @param   userId  the ID of the user to retrieve from the database
     * 
     * @return          the user with the specified ID
     */
    public User getUser( long userId ) {
        String where = User.ID_KEY + "=" + userId;
        Cursor cursor = database.query( USER_TABLE_NAME,
                new String[] { User.ID_KEY, User.USERNAME_KEY,
                User.SAVED_GAME_ID_KEY },
                where, null, null, null, null );
        
        // if we don't have any elements, return null
        if ( cursor.getCount() > 0 ) {
            cursor.moveToNext();
        } else {
            return null;
        }
        
        // create a new user
        User user = new User();
        user.setId( cursor.getLong( cursor.getColumnIndex( User.ID_KEY ) ) );
        user.setUsername( cursor.getString(
                cursor.getColumnIndex( User.USERNAME_KEY ) ) );
        
        // load the respective saved game 
        Cursor gameCursor = null;
        where = Game.ID_KEY + "=" + cursor.getLong(
                cursor.getColumnIndex( User.SAVED_GAME_ID_KEY ) );
        gameCursor = getGames( where );
        
        // if we don't have any elements, set the game to null
        if ( gameCursor.getCount() > 0 ) {
            gameCursor.moveToNext();
            
            // create a new game and populate it
            Game game = new Game();
            game.setUser( user );
            game.setId( gameCursor.getLong(
                    gameCursor.getColumnIndex( Game.ID_KEY ) ) );
            Calendar created = Calendar.getInstance();
            created.setTimeInMillis(
                    gameCursor.getLong( gameCursor.getColumnIndex(
                    Game.CREATED_KEY ) ) * MS_PER_S );
            game.setCreated( created );
            Calendar saved = Calendar.getInstance();
            saved.setTimeInMillis( gameCursor.getLong(
                    gameCursor.getColumnIndex( Game.SAVED_KEY ) ) * MS_PER_S );
            game.setSaved( saved );
            game.setLevel( gameCursor.getInt(
                    gameCursor.getColumnIndex( Game.LEVEL_KEY ) ) );
            game.setMoves( gameCursor.getInt(
                    gameCursor.getColumnIndex( Game.MOVES_KEY ) ) );
            game.setSeconds( gameCursor.getInt(
                    gameCursor.getColumnIndex( Game.SECONDS_KEY ) ) );
            
            int finishedInt = gameCursor.getInt(
                    gameCursor.getColumnIndex( Game.FINISHED_KEY ) );
            boolean finished = ( finishedInt == 1 );
            game.setFinished( finished );
            
            // query atom locations (column 2 = saved_game_id)
            where = Game.ATOM_GAME_ID_KEY + "=" + gameCursor.getLong(
                    gameCursor.getColumnIndex( Game.ID_KEY ) );
            Cursor atomCursor = database.query( ATOM_TABLE_NAME,
                    new String[] { Game.ATOM_MARKER_KEY, Game.ATOM_X_KEY,
                    Game.ATOM_Y_KEY }, where, null, null, null, null );
            
            // if we don't have any elements, ERROR
            if ( atomCursor.getCount() > 0 ) {
                
                // populate atom locations
                for ( int j = 0; j < atomCursor.getCount(); j++ ) {
                    atomCursor.moveToNext();
                    
                    short atomId = atomCursor.getShort(
                            atomCursor.getColumnIndex( Game.ATOM_MARKER_KEY ) );
                    int x = atomCursor.getInt(
                            atomCursor.getColumnIndex( Game.ATOM_X_KEY ) );
                    int y = atomCursor.getInt(
                            atomCursor.getColumnIndex( Game.ATOM_Y_KEY ) );
                    Point point = new Point( x, y );
                    
                    // add the atom location
                    game.getAtoms().put( atomId, point );
                }
            } else {
                // this should *never* happen
                assert false;
            }
            // set the game
            user.setCurrentGame( game );
            
            atomCursor.close();
        } else {
            user.setCurrentGame( null );
        }
        
        // close everything you open!
        gameCursor.close();
        cursor.close();
        
        return user;
    }
    
    /**
     * Returns a <tt>Cursor</tt> to all games saved under the specified user ID.
     * 
     * @param   userId  the ID of the user to retrieve games for
     * 
     * @return          a cursor of all games for the user with the specified ID
     */
    public Cursor getGames( long userId ) {
        return getGames( Game.USER_ID_KEY + "=" + userId );
    }
    
    /**
     * Returns a <tt>Cursor</tt> to all finished games saved under the specified
     * user ID.
     * 
     * @param   userId  the ID of the user to retrieve finished games for
     * 
     * @return          a cursor of all finished games for the user with the
     *                  specified ID
     */
    public Cursor getFinishedGames( long userId ) {
        return getGames( Game.USER_ID_KEY + "=" + userId + " AND "
                + Game.FINISHED_KEY + "=" + 1 );
    }
    
    /**
     * Find all games from the database that match the specified <tt>where</tt>
     * clause, if any.
     * 
     * @param   where   the SQL <tt>WHERE</tt> clause value
     * 
     * @return          a cursor of all games that match the specified where
     *                  clause
     */
    private Cursor getGames( String where ) {
        return database.query( GAME_TABLE_NAME, new String[] { Game.ID_KEY,
                Game.CREATED_KEY, Game.SAVED_KEY, Game.LEVEL_KEY,
                Game.MOVES_KEY, Game.SECONDS_KEY, Game.FINISHED_KEY }, where,
                null, null, null, null );
    }
    
    /**
     * Returns a <tt>Cursor</tt> specifically designed for the Main Menu to view
     * all saved users and their latest game.
     * 
     * @return  a cursor of all saved users and their latest levels
     */
    public Cursor getSavedUsers() {
        String sql = "SELECT USER.id AS '_id', USER.username AS '"
                + User.USERNAME_KEY + "', ( 'Level: ' || Game.level || ', "
                + "Last played: ' || strftime( '%m/%d/%Y %H:%M', Game.SAVED, "
                + "'unixepoch', 'localtime' ) ) AS '" + Game.SAVED_KEY
                + "' FROM USER, GAME WHERE USER." + User.SAVED_GAME_ID_KEY
                + " = GAME." + Game.ID_KEY + " ORDER BY GAME.saved DESC;";
        
        return database.rawQuery( sql, null );
    }
    
    /**
     * Deletes the user with the specified ID.  This method <tt>will</tt>
     * cascade and delete the user's games.
     * 
     * @param   userId  the ID of the user to be deleted
     */
    public void deleteUser( long userId ) {
        // delete the user first
        String where = User.ID_KEY + "=" + userId;
        database.delete( USER_TABLE_NAME, where, null );
        
        // delete all games and atoms
        Cursor games = getGames( userId );
        
        for ( int i = 0; i < games.getCount(); i++ ) {
            games.moveToNext();
            
            // get the game ID
            long gameId = games.getLong( games.getColumnIndex( Game.ID_KEY ) );
            deleteGame( gameId );
        }
    }
    
    /**
     * Deletes the game with the specified ID.  This method <tt>will</tt>
     * cascade and delete the game's atoms.
     * 
     * @param   gameId  the ID of the game to be deleted
     */
    public void deleteGame( long gameId ) {
        // delete the game
        String where = Game.ID_KEY + "=" + gameId;
        database.delete( GAME_TABLE_NAME, where, null );
        
        // delete all associated atoms (if any)
        deleteAtoms( gameId );
    }
    
    /**
     * Delete all the atoms associated with the specified game.
     * 
     * @param   gameId  the ID of the game to delete all atoms for
     */
    private void deleteAtoms( long gameId ) {
        String where = Game.ATOM_GAME_ID_KEY + "=" + gameId;
        database.delete( ATOM_TABLE_NAME, where, null );
    }
    
    /**
     * Returns whether the specified level has been completed.
     * 
     * @param   user    the user to check for a completed level
     * @param   level   the leve to check for completion
     * 
     * @return          returns <tt>true</tt> if the specified level has been
     *                  completed by the specified user, otherwise
     *                  <tt>false</tt>
     */
    public boolean isLevelCompleted( User user, int level ) {
        boolean retVal = false;
        
        String where = Game.USER_ID_KEY + "=" + user.getId() + " AND "
                + Game.LEVEL_KEY + "=" + level + " AND "
                + Game.FINISHED_KEY + "=1";
        Cursor cursor = database.query( GAME_TABLE_NAME,
                new String[] { Game.ID_KEY }, where, null, null, null, null );
        
        // if there isn't exactly one result, return false
        if ( cursor.getCount() == 1 ) {
            retVal = true;
        }
        cursor.close();
        
        return retVal;
    }
    
    /**
     * Deletes the specified saved level for the specified user.
     * 
     * @param   user    the user to delete a saved level for
     * @param   level   the saved level to be deleted
     */
    public void deleteSavedGame( User user, int level ) {
        String where = Game.USER_ID_KEY + "=" + user.getId();
        Cursor cursor = database.query( GAME_TABLE_NAME,
                new String[] { Game.ID_KEY }, where, null, null, null, null );
        
        // if there isn't exactly one result, return null
        if ( cursor.getCount() == 1 ) {
            cursor.moveToNext();
            
            int gameId = cursor.getInt( cursor.getColumnIndex( Game.ID_KEY ) );
            this.deleteGame( gameId );
        }
        cursor.close();
    }
    
    /**
     * A database helper used to create and upgrade the database when needed.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        /** The context from which this database helper was created. */
        private Context context;
        
        /**
         * Constructs a new database helper.
         * 
         * @param   context     the context from which the DB helper was started
         * @param   name        the name of the database
         * @param   factory     the cursor factory for this helper, if any
         * @param   version     the database version
         */
        public DatabaseHelper( Context context, String name,
                CursorFactory factory, int version ) {
            super( context, name, factory, version );
            this.context = context;
        }
        
        /**
         * Creates the database.
         * 
         * @param   db  the database to be created
         */
        @Override
        public void onCreate( SQLiteDatabase db ) {
            Resources res = context.getResources();
            
            // create the tables!
            db.execSQL( res.getString( R.string.sql_create_user_table ) );
            db.execSQL( res.getString( R.string.sql_create_game_table ) );
            db.execSQL( res.getString( R.string.sql_create_atom_table ) );
        }
        
        /**
         * No-op.  No database change at this time.
         * 
         * @param   db          the database to be updated
         * @param   oldVersion  the old version of the database
         * @param   newVersion  the new version of the database
         */
        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion,
                int newVersion ) {
            // implement this if I change the database!
        }
        
    } // DatabaseHelper
    
} // AtomixDBAdapter

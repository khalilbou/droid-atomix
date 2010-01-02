/*
 * LevelFileFormatException.java
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

/**
 * This exception is meant for format problems with Atomix level files.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class LevelFileFormatException extends IllegalArgumentException {
    
    /** The header for all errors this class represents. */
    private static final String HEADER = "Level File Format Exception: ";
    
    /**
     * Constructs a new <tt>LevelFileFormatException</t> with the specified
     * error message.
     * 
     * @param   msg     the exception's error message
     */
    public LevelFileFormatException( String msg ) {
        super( HEADER + msg );
    }
    
} // LevelFileFormatException

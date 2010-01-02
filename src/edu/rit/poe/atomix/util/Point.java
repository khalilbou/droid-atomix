/*
 * Point.java
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

package edu.rit.poe.atomix.util;

import java.io.Serializable;

/**
 * A serializable point class.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Point implements Serializable {
    
    /** The X coordinate. */
    public int x;
    
    /** The Y coordinate. */
    public int y;
    
    /**
     * Constructs a new <tt>Point</tt> (0,0).
     */
    public Point() {
        this( 0, 0 );
    }
    
    /**
     * Constructs a new <tt>Point</tt> at the specified location.
     * 
     * @param   x   the X coordinate
     * @param   y   the Y coordinate
     */
    public Point( int x, int y ) {
        set( x, y );
    }
    
    /**
     * Sets a new location of this object.
     * 
     * @param   x   the X coordinate
     * @param   y   the Y coordinate
     */
    public void set( int x, int y ) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Offsets this point's location by <tt>x</tt> and <tt>y</tt>.
     * 
     * @param   x   the X coordinate offset 
     * @param   y   the Y coordinate offset
     */
    public void offset( int x, int y ) {
        this.x += x;
        this.y += y;
    }
    
    /**
     * Returns <tt>true</tt> if this point matches the specified X and Y
     * coordiantes.
     * 
     * @param   x   the X coordinate
     * @param   y   the Y coordinate
     * 
     * @return      <tt>true</tt> if the specified coordinates are identical to
     *              this point, otherwise <tt>false</tt>
     */
    public boolean equals( int x, int y ) {
        return ( ( this.x == x ) && ( this.y == y ) );
    }
    
    /**
     * Returns <tt>true</tt> if this point matches the specified point.
     * 
     * @param   obj     the object to be compared to this one for equality
     * 
     * @return          <tt>true</tt> if the specified object equals this one,
     *                  otherwise <tt>false</tt>
     */
    @Override
    public boolean equals( Object obj ) {
        boolean retVal = false;
        if ( obj instanceof Point ) {
            Point p = ( Point )obj;
            retVal = equals( p.x, p.y );
        }
        return retVal;
    }
    
    /**
     * Returns a hashcode of this point.
     * 
     * @return  the hashcode of this point's coordinates.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.x;
        hash = 23 * hash + this.y;
        return hash;
    }
    
} // Point

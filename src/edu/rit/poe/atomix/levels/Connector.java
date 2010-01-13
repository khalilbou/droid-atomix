/*
 * Connector.java
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

import java.io.Serializable;
import java.util.EnumSet;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Connector implements Serializable {
    
    public static enum Direction {
        
        UP( "u" ),
        
        DOWN( "d" ),
        
        RIGHT( "r" ),
        
        LEFT( "l" ),
        
        UPPER_RIGHT( "ur" ),
        
        UPPER_LEFT( "ul" ),
        
        LOWER_RIGHT( "lr" ),
        
        LOWER_LEFT( "ll" );
        
        private String name;
        
        Direction( String name ) {
            this.name = name;
        }
        
        static Direction parse( String str ) {
            Direction direction = null;
            
            for ( Direction dir : EnumSet.allOf( Direction.class ) ) {
                if ( str.equalsIgnoreCase( dir.name ) ) {
                    direction = dir;
                }
            }
            
            return direction;
        }
        
    } // Direction
    
    public static enum Bond {
        
        SINGLE,
        
        DOUBLE;
        
    } // Bond
    
    private Direction direction;
    
    private Bond bond;
    
    /**
     * Constructs a new <tt>Connector</tt>.
     * @param direction
     * @param bond
     */
    public Connector( Direction direction, Bond bond ) {
        this.direction = direction;
        this.bond = bond;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public Bond getBond() {
        return bond;
    }
    
    @Override
    public boolean equals( Object o ) {
        boolean retVal = false;
        if ( o instanceof Connector ) {
            Connector c = ( Connector )o;
            retVal = ( ( c.direction == direction ) && ( c.bond == bond ) );
        }
        return retVal;
    }
    
    @Override
    public int hashCode() {
        return direction.hashCode() + bond.hashCode();
    }
    
} // Connector

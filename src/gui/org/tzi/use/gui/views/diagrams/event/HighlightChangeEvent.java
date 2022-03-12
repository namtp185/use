/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

// $Id: HighlightChangeEvent.java 1050 2009-07-07 16:25:22Z lars $

package org.tzi.use.gui.views.diagrams.event;

import java.util.EventObject;

import org.tzi.use.uml.mm.MModelElement;


/**
 * A HighlightChangeEvent is used to notify interested listeners that 
 * another ModelElement is selected.
 */
@SuppressWarnings("serial")
public class HighlightChangeEvent extends EventObject {
    private MModelElement fElement;
    private boolean fHighlight;
    
    public HighlightChangeEvent( Object source ) {
        super( source );
    }
    
    public void setModelElement( MModelElement elem ) {
        fElement = elem;
    }
    public MModelElement getModelElement() {
        return fElement;
    }    
    
    public void setHighlight( boolean highlight ) {
        fHighlight = highlight;
    }
    public boolean getHighlight() {
        return fHighlight;
    }
}


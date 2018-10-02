// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package org.infinity.search;

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infinity.datatype.ResourceRef;
import org.infinity.resource.AbstractStruct;
import org.infinity.resource.Resource;
import org.infinity.resource.StructEntry;
import org.infinity.resource.are.Actor;
import org.infinity.resource.are.Container;
import org.infinity.resource.are.Door;
import org.infinity.resource.are.ITEPoint;
import org.infinity.resource.bcs.BcsResource;
import org.infinity.resource.cre.CreResource;
import org.infinity.resource.dlg.AbstractCode;
import org.infinity.resource.key.ResourceEntry;
import org.infinity.resource.text.PlainTextResource;

public final class ScriptReferenceSearcher extends AbstractReferenceSearcher
{
  public ScriptReferenceSearcher(ResourceEntry targetEntry, Component parent)
  {
    super(targetEntry, new String[]{"ARE", "BCS", "CHR", "CRE", "DLG", "INI"}, parent);
  }

  @Override
  protected void search(ResourceEntry entry, Resource resource)
  {
    if (resource instanceof BcsResource) {
      // passing raw bytecode to improve performance
      searchScript(entry, ((BcsResource)resource).getCode(), null);
    } else if (resource instanceof PlainTextResource) {
      searchText(entry, ((PlainTextResource)resource).getText());
    } else {
      searchStruct(entry, (AbstractStruct)resource);
    }
  }

  private void searchStruct(ResourceEntry entry, AbstractStruct struct)
  {
    for (int i = 0; i < struct.getFieldCount(); i++) {
      StructEntry o = struct.getField(i);
      if (o instanceof ResourceRef &&
          ((ResourceRef)o).getResourceName().equalsIgnoreCase(targetEntry.toString())) {
        ResourceRef ref = (ResourceRef)o;
        if (struct instanceof CreResource) {
          addHit(entry, entry.getSearchString(), ref);
        } else if (struct instanceof Actor) {
          addHit(entry, struct.getField(20).toString(), ref);
        } else {
          addHit(entry, null, ref);
        }
      }
      else if (o instanceof Actor ||
               o instanceof Container ||
               o instanceof Door ||
               o instanceof ITEPoint) {
        searchStruct(entry, (AbstractStruct)o);
      }
      else if (o instanceof AbstractCode) {
        searchScript(entry, o.toString(), o);
      }
    }
  }

  private void searchText(ResourceEntry entry, String text)
  {
    String name = getTargetEntry().getResourceName();
    int idx = name.lastIndexOf('.');
    if (idx > 0) {
      name = name.substring(0, idx);
    }
    final Pattern p = Pattern.compile("\\b" + name + "\\b", Pattern.CASE_INSENSITIVE);
    final Matcher m = p.matcher(text);
    if (m.find()) {
      addHit(entry, entry.getSearchString(), null);
    }
  }

  private void searchScript(ResourceEntry entry, String script, StructEntry ref)
  {
    String name = getTargetEntry().getResourceName();
    int idx = name.lastIndexOf('.');
    if (idx > 0) {
      name = name.substring(0, idx);
    }
    final Pattern p = Pattern.compile("\"" + name + "\"", Pattern.CASE_INSENSITIVE);
    final Matcher m = p.matcher(script);
    if (m.find()) {
      addHit(entry, entry.getSearchString(), ref);
    }
  }
}

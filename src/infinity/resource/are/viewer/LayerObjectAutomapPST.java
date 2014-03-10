// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.resource.are.viewer;

import java.awt.Image;
import java.awt.Point;

import infinity.datatype.DecNumber;
import infinity.datatype.TextString;
import infinity.gui.layeritem.AbstractLayerItem;
import infinity.gui.layeritem.IconLayerItem;
import infinity.icon.Icons;
import infinity.resource.AbstractStruct;
import infinity.resource.are.AreResource;
import infinity.resource.are.AutomapNotePST;

/**
 * Handles specific layer type: ARE/Automap Note (PST-specific)
 * @author argent77
 */
public class LayerObjectAutomapPST extends LayerObject
{
  private static final Image[] Icon = new Image[]{Icons.getImage("itm_Automap1.png"),
                                                  Icons.getImage("itm_Automap2.png")};
  private static Point Center = new Point(26, 26);
  private static final double MapScale = 32.0 / 3.0;    // scaling factor for MOS to TIS coordinates

  private final AutomapNotePST note;
  private final Point location = new Point();

  private IconLayerItem item;


  public LayerObjectAutomapPST(AreResource parent, AutomapNotePST note)
  {
    super(ViewerConstants.RESOURCE_ARE, "Automap", AutomapNotePST.class, parent);
    this.note = note;
    init();
  }

  @Override
  public AbstractStruct getStructure()
  {
    return note;
  }

  @Override
  public AbstractStruct[] getStructures()
  {
    return new AbstractStruct[]{note};
  }

  @Override
  public AbstractLayerItem getLayerItem()
  {
    return item;
  }

  @Override
  public AbstractLayerItem getLayerItem(int type)
  {
    return (type == 0) ? item : null;
  }

  @Override
  public AbstractLayerItem[] getLayerItems()
  {
    return new AbstractLayerItem[]{item};
  }

  @Override
  public void reload()
  {
    init();
  }

  @Override
  public void update(double zoomFactor)
  {
    if (note != null) {
      item.setItemLocation((int)(location.x*zoomFactor + (zoomFactor / 2.0)),
                           (int)(location.y*zoomFactor + (zoomFactor / 2.0)));
    }
  }

  @Override
  public Point getMapLocation()
  {
    return location;
  }

  @Override
  public Point[] getMapLocations()
  {
    return new Point[]{location};
  }

  private void init()
  {
    if (note != null) {
      String msg = "";
      try {
        int v = ((DecNumber)note.getAttribute("Coordinate: X")).getValue();
        location.x = (int)(v * MapScale);
        v = ((DecNumber)note.getAttribute("Coordinate: Y")).getValue();
        location.y = (int)(v * MapScale);
        msg = ((TextString)note.getAttribute("Text")).toString();
      } catch (Exception e) {
        e.printStackTrace();
      }

      // Using cached icons
      Image[] icon;
      String keyIcon = String.format("%1$s%2$s", SharedResourceCache.createKey(Icon[0]),
                                                 SharedResourceCache.createKey(Icon[1]));
      if (SharedResourceCache.contains(SharedResourceCache.Type.Icon, keyIcon)) {
        icon = ((ResourceIcon)SharedResourceCache.get(SharedResourceCache.Type.Icon, keyIcon)).getData();
        SharedResourceCache.add(SharedResourceCache.Type.Icon, keyIcon);
      } else {
        icon = Icon;
        SharedResourceCache.add(SharedResourceCache.Type.Icon, keyIcon, new ResourceIcon(keyIcon, icon));
      }

      item = new IconLayerItem(location, note, msg, icon[0], Center);
      item.setName(getCategory());
      item.setToolTipText(msg);
      item.setImage(AbstractLayerItem.ItemState.HIGHLIGHTED, icon[1]);
      item.setVisible(isVisible());
    }
  }
}

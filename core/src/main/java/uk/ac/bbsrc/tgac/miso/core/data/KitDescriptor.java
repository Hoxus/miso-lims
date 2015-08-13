/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.core.data;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.bbsrc.tgac.miso.core.data.type.KitType;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;

import java.math.BigDecimal;

/**
 * A KitDescriptor handles information about a consumable element that is generally typed by a name, manufacturer and part number. KitComponents use
 * KitComponentsDescriptors, which in turn use KitDescriptors to represent a real-world manifestation of a consumable kit.
 *
 * @author Rob Davey, Michal Zak
 * @since 0.0.2
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC, include = JsonSerialize.Inclusion.NON_NULL)
//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public interface KitDescriptor extends Nameable {
  /**
   * Returns the units in which we measure this KitDescriptor object
   * @return units String
   */
  String getUnits();

  /**
   * Sets the units in which we measure this KitDescriptor object
   * @param units units
   */
   void setUnits(String units);

  /**
   * Returns the monetary value of this KitDescriptor object in pounds
   * @return value float
   */
  BigDecimal getKitValue();

  /**
   * Sets the monetary value of this KitDescriptor object in pounds
   * mySQL:: FLOAT(7,2) precision
   * @param kitValue value
   */
  void setKitValue(BigDecimal kitValue);

  /**
   * Sets the kitDescriptorId of this KitDescriptor object.
   *
   * @param kitDescriptorId kitDescriptorId.
   */
   void setId(long kitDescriptorId);

  /**
   * Sets the name of this KitDescriptor object.
   *
   * @param name name.
   */
   void setName(String name);

  /**
   * Returns the version of this KitDescriptor object.
   *
   * @return Double version.
   */
   Double getVersion();

  /**
   * Sets the version of this KitDescriptor object.
   *
   * @param version version.
   */
   void setVersion(Double version);

  /**
   * Returns the manufacturer of this KitDescriptor object.
   *
   * @return String manufacturer.
   */
   String getManufacturer();

  /**
   * Sets the manufacturer of this KitDescriptor object.
   *
   * @param manufacturer manufacturer.
   */
   void setManufacturer(String manufacturer);

  /**
   * Returns the partNumber of this KitDescriptor object.
   *
   * @return String partNumber.
   */
   String getPartNumber();

  /**
   * Sets the partNumber of this KitDescriptor object.
   *
   * @param partNumber partNumber.
   */
   void setPartNumber(String partNumber);

  /**
   * Returns the stockLevel of this KitDescriptor object.
   *
   * @return Integer stockLevel.
   */
   KitType getKitType();

  /**
   * Sets the kitType of this KitDescriptor object.
   *
   * @param kitType kitType.
   *
   */
   void setKitType(KitType kitType);

  /**
   * Returns the platformType of this KitDescriptor object.
   *
   * @return PlatformType platformType.
   */
  PlatformType getPlatformType();

  /**
   * Sets the platformType of this KitDescriptor object.
   *
   * @param platformType platformType.
   */
  void setPlatformType(PlatformType platformType);

  /**
   * Method toString ...
   * @return String
   */
  @Override
  String toString();
}

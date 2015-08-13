/*
 * Copyright (c) 2015. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey @ TGAC
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

package uk.ac.bbsrc.tgac.miso.spring.ajax;

import net.sf.json.JSONArray;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import uk.ac.bbsrc.tgac.miso.core.data.*;
import com.eaglegenomics.simlims.core.User;
import com.eaglegenomics.simlims.core.manager.SecurityManager;
import net.sf.json.JSONObject;

import net.sourceforge.fluxion.ajax.Ajaxified;
import net.sourceforge.fluxion.ajax.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.bbsrc.tgac.miso.core.data.impl.kit.KitComponentImpl;
import uk.ac.bbsrc.tgac.miso.core.util.*;
import uk.ac.bbsrc.tgac.miso.core.manager.RequestManager;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.*;
import java.util.*;

/**
 * uk.ac.bbsrc.tgac.miso.spring.ajax
 * <p/>
 * Info
 *
 * @author Michal Zak
 * @since 0.0.2
 */
@Ajaxified
public class KitComponentControllerHelperService {
  protected static final Logger log = LoggerFactory.getLogger(KitComponentControllerHelperService.class);

  @Autowired
  private SecurityManager securityManager;
  @Autowired
  private RequestManager requestManager;

  public int countNonExhaustedComponentsByKitComponentDescriptorId(long kitComponentDescriptorId){
    int count = 0;
    try{
      Collection<KitComponent> kitComponents = requestManager.listKitComponentsByKitComponentDescriptorId(kitComponentDescriptorId);

      for(KitComponent kitComponent: kitComponents){
        if(!kitComponent.isExhausted()){
          count++;
        }
      }
      return count;

    }catch (IOException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public JSONObject listKitComponentDescriptorsByKitDescriptorId(HttpSession session, JSONObject json){
    JSONArray componentDescriptorsArr = new JSONArray();

    long kitDescriptorId = json.getLong("kitDescriptorId");

    try{
      Collection<KitComponentDescriptor> kitComponentDescriptors = requestManager.listKitComponentDescriptorsByKitDescriptorId(kitDescriptorId);
      JSONObject componentDescriptor = new JSONObject();

      for(KitComponentDescriptor kitComponentDescriptor : kitComponentDescriptors){
        componentDescriptor.put("kitComponentDescriptorId", kitComponentDescriptor.getId());
        componentDescriptor.put("name", kitComponentDescriptor.getName());
        componentDescriptor.put("referenceNumber", kitComponentDescriptor.getReferenceNumber());

        int stockLevel = countNonExhaustedComponentsByKitComponentDescriptorId(kitComponentDescriptor.getId());
        componentDescriptor.put("stockLevel", stockLevel);
        componentDescriptorsArr.add(componentDescriptor);
      }

      JSONObject jsonDescriptors = new JSONObject();
      jsonDescriptors.put("componentDescriptors", componentDescriptorsArr);
      return jsonDescriptors;
    } catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("error");
    }
  }

  public JSONObject listAllKitDescriptors(HttpSession session, JSONObject json){
    JSONArray descriptorsArr = new JSONArray();

    try{
      Collection<KitDescriptor> kitDescriptors = requestManager.listAllKitDescriptors();

      JSONObject descriptor = new JSONObject();
      NumberFormat formatter = new DecimalFormat("£#0.00");

      for(KitDescriptor kitDescriptor : kitDescriptors){
        descriptor.put("kitDescriptorId", kitDescriptor.getId());
        descriptor.put("name", kitDescriptor.getName());
        descriptor.put("version", kitDescriptor.getVersion());
        descriptor.put("manufacturer", kitDescriptor.getManufacturer());
        descriptor.put("partNumber", kitDescriptor.getPartNumber());
        descriptor.put("kitType", kitDescriptor.getKitType().getKey());
        descriptor.put("platformType", kitDescriptor.getPlatformType().getKey());
        descriptor.put("units", kitDescriptor.getUnits());
        descriptor.put("kitValue", formatter.format(kitDescriptor.getKitValue()));

        descriptorsArr.add(descriptor);
      }

      JSONObject jsonDescriptors = new JSONObject();
      jsonDescriptors.put("descriptors", descriptorsArr);    //nesting might not be necessary
      return jsonDescriptors;
    } catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("error");
    }
  }

  public JSONObject listAllKitComponentsForTable(HttpSession session, JSONObject json){
    JSONArray componentsArr = new JSONArray();
    try {
      Collection<KitComponent> kitComponents = requestManager.listAllKitComponents();
      KitComponentDescriptor kitComponentDescriptor;
      KitDescriptor kitDescriptor;

      JSONObject component = new JSONObject();
      NumberFormat formatter = new DecimalFormat("£#0.00");

      for (KitComponent kitComponent : kitComponents){
        kitComponentDescriptor = kitComponent.getKitComponentDescriptor();
        kitDescriptor = kitComponentDescriptor.getKitDescriptor();

        component.put("ID", kitComponent.getId());
        component.put("Kit Name", kitDescriptor.getName());
        component.put("Component Name", kitComponentDescriptor.getName());
        component.put("Version", kitDescriptor.getVersion());
        component.put("Manufacturer", kitDescriptor.getManufacturer());
        component.put("Part Number", kitDescriptor.getPartNumber());
        component.put("Type", kitDescriptor.getKitType());
        component.put("Platform", kitDescriptor.getPlatformType());
        component.put("Units", kitDescriptor.getUnits());
        component.put("Value", formatter.format(kitDescriptor.getKitValue()));
        component.put("Reference Number", kitComponentDescriptor.getReferenceNumber());
        component.put("Identification Barcode", kitComponent.getIdentificationBarcode());
        component.put("Lot Number", kitComponent.getLotNumber());
        component.put("Location Barcode", kitComponent.getLocationBarcode());
        component.put("Received Date", kitComponent.getKitReceivedDate().toString(ISODateTimeFormat.date()));
        component.put("Expiry Date", kitComponent.getKitExpiryDate().toString(ISODateTimeFormat.date()));
        //JAVA 8
        //component.put("Received Date", kitComponent.getKitReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        //component.put("Expiry Date", kitComponent.getKitExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        component.put("Exhausted", kitComponent.isExhausted());
        component.put("Expiry State", DateUtils.getExpiryState(kitComponent.getKitExpiryDate()).ordinal());
        componentsArr.add(component);
      }
      JSONObject jsonComponents = new JSONObject();
      jsonComponents.put("components", componentsArr);    //nesting might not be necessary
      return jsonComponents;
    }
    catch (IOException ex) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to list kit components", ex);
      }
      return JSONUtils.SimpleJSONError("error");
    }
  }

  public JSONObject getKitInfoByReferenceNumber(HttpSession session, JSONObject json) {
    try {
      JSONObject response = new JSONObject();
      KitComponentDescriptor kitComponentDescriptor =  requestManager.getKitComponentDescriptorByReferenceNumber(json.getString("referenceNumber"));

      if(kitComponentDescriptor !=null) {
        KitDescriptor kitDescriptor = kitComponentDescriptor.getKitDescriptor();

        //kit descriptor info
        response.put("name", kitDescriptor.getName());
        response.put("version", kitDescriptor.getVersion());
        response.put("manufacturer", kitDescriptor.getManufacturer());
        response.put("partNumber", kitDescriptor.getPartNumber());
        response.put("kitType", kitDescriptor.getKitType().getKey());
        response.put("platformType", kitDescriptor.getPlatformType().getKey());
        response.put("units", kitDescriptor.getUnits());
        response.put("kitValue", kitDescriptor.getKitValue());

        //kit component descriptor info
        response.put("componentName", kitComponentDescriptor.getName());
      }
      return response;
    }
    catch (IOException e) {
      log.debug("Failed", e);
      return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
    }
  }

  public JSONObject getKitInfoByIdentificationBarcode(HttpSession session, JSONObject json) {
    try {
      JSONObject response = new JSONObject();
      KitComponent kitComponent =  requestManager.getKitComponentByIdentificationBarcode(json.getString("identificationBarcode"));

      if(kitComponent != null) {
        KitComponentDescriptor kitComponentDescriptor = kitComponent.getKitComponentDescriptor();
        KitDescriptor kitDescriptor = kitComponentDescriptor.getKitDescriptor();

        //kit descriptor info
        response.put("name", kitDescriptor.getName());
        //kit component descriptor info
        response.put("componentName", kitComponentDescriptor.getName());
        response.put("referenceNumber", kitComponentDescriptor.getReferenceNumber());
        //kit component info
        response.put("lotNumber", kitComponent.getLotNumber());
        response.put("receivedDate", kitComponent.getKitReceivedDate().toString());
        response.put("expiryDate", kitComponent.getKitExpiryDate().toString());
        response.put("locationBarcode", kitComponent.getLocationBarcode());
        response.put("exhausted", kitComponent.isExhausted());
        response.put("kitComponentId", kitComponent.getId());
      }
      return response;
    }
    catch (IOException e) {
      log.debug("Failed", e);
      return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
    }
  }

  public JSONObject exhaustKitComponent(HttpSession session, JSONObject json){
    String identificationBarcode = json.getString("identificationBarcode");
    String locationBarcode = json.getString("locationBarcodeNew");

    try {
      KitComponent kitComponent = requestManager.getKitComponentByIdentificationBarcode(identificationBarcode);
      kitComponent.setExhausted(true);
      kitComponent.setLocationBarcode(locationBarcode);
      requestManager.saveKitComponent(kitComponent);

      //log the change
      JSONObject logObject = new JSONObject();
      logObject.put("kitComponentId", kitComponent.getId());
      logObject.put("locationBarcodeNew", json.getString("locationBarcodeNew"));
      logObject.put("locationBarcodeOld", json.getString("locationBarcodeOld"));
      logObject.put("exhausted", true);

      //LOG
      logLocationChange(logObject);
    } catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("Cannot exhaust kit component: " + e.getMessage());
    }
    return JSONUtils.SimpleJSONResponse("ok");
  }

  public JSONObject changeLocation(HttpSession session, JSONObject json){
    String identificationBarcode = json.getString("identificationBarcode");
    String locationBarcode = json.getString("locationBarcodeNew");

    try {
      KitComponent kitComponent = requestManager.getKitComponentByIdentificationBarcode(identificationBarcode);
      kitComponent.setLocationBarcode(locationBarcode);
      requestManager.saveKitComponent(kitComponent);

      //log the change
      JSONObject logObject = new JSONObject();
      logObject.put("kitComponentId", kitComponent.getId());
      logObject.put("locationBarcodeNew", json.getString("locationBarcodeNew"));
      logObject.put("locationBarcodeOld", json.getString("locationBarcodeOld"));
      logObject.put("exhausted", false);
      //LOG
      logLocationChange(logObject);
    } catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("Cannot change kit location: " + e.getMessage());
    }
    return JSONUtils.SimpleJSONResponse("ok");
  }

  public void logLocationChange (JSONObject json) throws IOException {
    User user = securityManager.getUserByLoginName(SecurityContextHolder.getContext().getAuthentication().getName());
    long userId = user.getUserId();

    LocalDateTime now = LocalDateTime.now();
    Timestamp nowTimestamp = Timestamp.valueOf(now.toString());

    json.put("userId", userId);
    json.put("logDate", nowTimestamp);

    try{
      requestManager.saveKitChangeLog(json);
    }catch (IOException e){
      e.printStackTrace();
    }
  }

  public JSONObject isKitComponentAlreadyLogged(HttpSession session, JSONObject json) throws IOException{
    String identificationBarcode = json.getString("identificationBarcode");
    boolean isLogged = false;

    try {
      isLogged = requestManager.isKitComponentAlreadyLogged(identificationBarcode);
    }catch (IOException e){
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("Cannot check kit logged status: " + e.getMessage());
    }

    JSONObject result = new JSONObject();
    result.put("isLogged", isLogged);
    return result;
  }

  public JSONObject saveKitComponents(HttpSession session, JSONObject json){
    JSONArray a = JSONArray.fromObject(json.get("components"));

    for (JSONObject j : (Iterable<JSONObject>) a) {
      KitComponent kitComponent = new KitComponentImpl();

      String identificationBarcode = j.getString("identificationBarcode");
      String locationBarcode = j.getString("locationBarcode");
      String lotNumber = j.getString("lotNumber");
      LocalDate receivedDate = DateUtils.asLocalDate(j.getString("receivedDate"));
      LocalDate expiryDate = DateUtils.asLocalDate(j.getString("expiryDate"));
      String referenceNumber = j.getString("referenceNumber");

      kitComponent.setIdentificationBarcode(identificationBarcode);
      kitComponent.setLocationBarcode(locationBarcode);
      kitComponent.setLotNumber(lotNumber);
      kitComponent.setKitReceivedDate(receivedDate);
      kitComponent.setKitExpiryDate(expiryDate);
      kitComponent.setExhausted(false);

      try {
        kitComponent.setKitComponentDescriptor(requestManager.getKitComponentDescriptorByReferenceNumber(referenceNumber));
        requestManager.saveKitComponent(kitComponent);

        //log the change
        JSONObject logObject = new JSONObject();
        logObject.put("kitComponentId", kitComponent.getId());
        logObject.put("locationBarcodeNew", locationBarcode);
        logObject.put("locationBarcodeOld", "N/A");
        logObject.put("exhausted", false);
        //LOG
        logLocationChange(logObject);
      } catch (IOException e) {
        e.printStackTrace();
        return JSONUtils.SimpleJSONError("Something went wrong");
      }
    }
    return JSONUtils.SimpleJSONResponse("ok");
  }

  public JSONObject getKitChangeLog(HttpSession session, JSONObject json){
    JSONObject result = new JSONObject();
    try {
      JSONArray changeLog = requestManager.getKitChangeLog();
      result.put("changeLog", changeLog);
    }catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("Cannot retrieve kit change log: " + e.getMessage());
    }
    return result;
  }

  public JSONObject getKitChangeLogByKitComponentId(HttpSession session, JSONObject json){
    JSONObject result = new JSONObject();
    try {
      JSONArray changeLog = requestManager.getKitChangeLogByKitComponentId(json.getLong("kitComponentId"));
      result.put("changeLog", changeLog);
    }catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("Cannot retrieve kit change log: " + e.getMessage());
    }
    return result;
  }

  public JSONObject getKitDescriptorByPartNumber(HttpSession session, JSONObject json){
    JSONObject result = new JSONObject();
    try {
      KitDescriptor kitDescriptor  = requestManager.getKitDescriptorByPartNumber(json.getString("partNumber"));
      result.put("kitDescriptor", kitDescriptor);
    }catch (IOException e) {
      e.printStackTrace();
      return JSONUtils.SimpleJSONError("Cannot retrieve kit descriptor: " + e.getMessage());
    }
    return result;
  }

  public void setSecurityManager(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  public void setRequestManager(RequestManager requestManager) {
    this.requestManager = requestManager;
  }
}

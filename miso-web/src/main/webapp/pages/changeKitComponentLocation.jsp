<%@ include file="../header.jsp" %>

<%--
  ~ Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
  ~ MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
  ~ **********************************************************************
  ~
  ~ This file is part of MISO.
  ~
  ~ MISO is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ MISO is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with MISO.  If not, see <http://www.gnu.org/licenses/>.
  ~
  ~ **********************************************************************
  --%>

<div id="maincontent">
  <div id="contentcolumn">
    <h1>Change Kit's Location</h1>

    <div id="kitInfo">
    </div>
    <div id="identificationBarcodeForm">
      Scan the identification barcode and press Enter
      <input type="text" name="identificationBarcode" id="identificationBarcode"/>
    </div>

    <div id="locationBarcodeForm" style="display:none">
      New location:
      <input type="text" name="locationBarcode" id="locationBarcode"/>
    </div>
    <br>
    <button type="button" id="changeLocationButton" style="display:none">Change Location</button>
  </div>
</div>

<script>
  var identificationBarcode;
  var locationBarcodeOld;

  jQuery(document).ready(function(){
    jQuery("#identificationBarcode").focus();
  });

  //press enter to show the rest of the form
  jQuery("#identificationBarcode").keypress(function(e){
    if(e.which==13){
      identificationBarcode = jQuery(this).val();
      getKitInfoByIdentificationNumber();
      jQuery("#locationBarcode").focus();
    }
  });

  jQuery("#changeLocationButton").click(function(){
    changeKitLocation();
  });

  function changeKitLocation(){
    var locationBarcodeNew = jQuery("#locationBarcode").val();

    Fluxion.doAjax(
      'kitComponentControllerHelperService',
      'changeLocation',

      {
        'identificationBarcode':identificationBarcode,
        'locationBarcodeNew': locationBarcodeNew,
        'locationBarcodeOld': locationBarcodeOld,
        'url': ajaxurl
      },
      {'doOnSuccess': function (json) {
        alert("The kit component has been successfully relocated.");
        location.reload(true);
      }
    });
  }

  function getKitInfoByIdentificationNumber(){
    identificationBarcode = jQuery("#identificationBarcode").val();

    Fluxion.doAjax(
      'kitComponentControllerHelperService',
      'getKitInfoByIdentificationBarcode',

      {
        'identificationBarcode':identificationBarcode,
        'url': ajaxurl
      },
      {'doOnSuccess': function (json) {
        if(jQuery.isEmptyObject(json)){
          alert("The identification barcode is not recognised");
        } else if (json.exhausted) {
          alert("This kit has already been exhausted");
        } else {
          var htmlStrResult = "<h2>Kit Info </h2><br><b>Name:</b> \t" + json.name +" " + json.componentName +  "<br>" +
          "<b>Reference Number:</b> \t" + json.referenceNumber + "<br>" +
          "<b>Lot Number:</b> \t" + json.lotNumber + "<br>" +
          "<b>Received Date:</b> \t" + json.receivedDate + "<br>" +
          "<b>Expiry Date:</b> \t" + json.expiryDate + "<br>" +
          "<b>Location Barcode:</b> \t" + json.locationBarcode + "<br><br><br>";

          jQuery("#identificationBarcodeForm").hide();
          jQuery("#locationBarcodeForm").show();
          jQuery("#changeLocationButton").show();

          jQuery('#kitInfo').html(htmlStrResult);

          jQuery('#kitInfo').show();

          locationBarcodeOld = json.locationBarcode;
        }
      }
    });
  };
</script>
<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>
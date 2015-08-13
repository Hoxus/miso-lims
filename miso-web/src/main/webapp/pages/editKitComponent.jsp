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
<%@ include file="../header.jsp" %>
<div id="maincontent">
<div id="contentcolumn">
    <h1>Log Kit Component(s)</h1>
    <div id="locationEntered"></div>
    <div id="kitInfo" style="display: none;"></div>
    <div id="locationForm">
      Scan location barcode and press Enter to start the logging session
      <table class="in">
        <tr>
          <td class="h">Location Barcode</td>
          <td><input type="text" id="locationBarcode" name="locationBarcode"/></td>
        </tr>
      </table>
    </div>

    <div id="addComponent" style="display:none">
    <form action="#" name="addComponent" id="addComponentForm">
      <table class="in">
        <tr>
          <td class="h">Scan REF barcode</td>
          <td><input type="text" id="referenceNumber" name="referenceNumber"/>
        </tr>
        <tr>
          <td class="h">Scan LOT barcode:</td>
          <td><input type="text" id="lotNumber" name="lotNumber"/></td>
        </tr>
        <tr>
          <td class="h">Scan identification barcode:</td>
          <td><input type="text" id="identificationBarcode" name="identificationBarcode"/></td>
        </tr>
        <tr>
          <td class="h">Enter expiry date:</td>
          <td><input  type="text" id="expiryDate" name="expiryDate" class="date-picker-element"/></td>
        </tr>
        <tr>
          <td class="h">Received date (<i>default:today</i>):</td>
          <td><input  type="text" id="receivedDate" name="receivedDate" class="date-picker-element"/></td>
        </tr>
      </table>

      <button type="button" name="submit" id="addComponentButton">Add</button>
    </form>

    <h1>Components already added</h1>
    <button type='button' id="saveKits" style="display:none">Save</button>
    <div id="componentsList">
      <span id="noComponents"><i>You haven't added any components yet</i></span>
      <ol type="1" id="orderedComponentsList"></ol>
    </div>
</div>

<script type="text/javascript">
  var currentComponentFullName;
  var locationBarcode;
  var components = [];

  jQuery(document).ready(function(){
      jQuery("#locationBarcode").focus();
  });

  //datepicker
  jQuery(function() {
    jQuery( ".date-picker-element" ).datepicker({
        dateFormat: "yy-mm-dd",
        defaultDate: 0
      }
    );

    jQuery("#receivedDate").datepicker('setDate', new Date());
  });

  //prevent from submitting form with empty fields
  jQuery("#addComponentButton").click(function() {
    var empty = jQuery(this).parent().find("input").filter(function() {
      return this.value === "";
    });
    if(empty.length) {
      alert("Please fill out all the fields");
    }else{
      addKitComponent();
    }
  });

  //show/hide component details
  jQuery("#componentsList").on('click', ".collapsedComponent", function(){
      jQuery(this).children().toggle();
  });

  //press enter to show the rest of the form
  jQuery("#locationBarcode").keypress(function(e){
    if(e.which==13){
      locationBarcode = jQuery(this).val();

      jQuery("#locationForm").hide();
      jQuery("#locationEntered").html("<h2>Storage Location</h2><br>" + jQuery("#locationBarcode").val() + "<br> <br>");
      jQuery("#addComponent").show();
      jQuery("#referenceNumber").focus();
    }
  });

  jQuery("#addComponentForm").keypress(function(e){
    if(e.which==13){
      jQuery("#addComponentButton").click();
    }
  });

  //show the kit info based on reference number
  jQuery("#referenceNumber").blur(function(){
    if(jQuery(this).val() != ""){
      getKitInfoByReferenceNumber();
    }
  });

  jQuery('#saveKits').click(function(){
    saveKitComponents();
  });

  function addKitComponent(){
    jQuery('#saveKits').show();

    var component ={};
    var fields = jQuery("#addComponentForm").serializeArray();  //get the data from the form

    //convert the data into JSON object
    jQuery.each(fields, function(){
      if(component[this.name] !== undefined){
        if(component[this.name].push){
          component[this.name] = [component[this.name]];
        }
        component[this.name].push(this.value || "");
      }else{
        component[this.name] =this.value || "";
      }
    });

    //add two more keys/values
    component.fullName = currentComponentFullName;
    component.locationBarcode = locationBarcode;

    //add to the components list
    components.push(component);

    //INFO DISPLAY
    //we have some components - remove the text
    jQuery("#noComponents").html("");

    //create a div holding the name of the component
    var listElement = document.createElement('li');
    var collapsedDiv = document.createElement('div');
    collapsedDiv.className="collapsedComponent";
    //collapsedDiv.id="collapsedComponent" + components.length;
    listElement.appendChild(collapsedDiv);
    jQuery('#orderedComponentsList').appendChild(listElement);

    var htmlComponentDetails = "<b>Identification Barcode:</b> \t" + component.identificationBarcode + "<br>" +
            "<b>Lot Number:</b> \t" + component.lotNumber + "<br>" +
            "<b>Expiry Date:</b> \t" + component.expiryDate + "<br>" +
            "<b>Received Date:</b> \t" + component.receivedDate + "<br>" +
            "<b>Reference Number:</b> \t" + component.referenceNumber + "<br>";

    //add the name
    jQuery(collapsedDiv).html(component.fullName);

    //create an inner div holding the rest of the details
    var expandedDiv = document.createElement('div');
    expandedDiv.className="expandedComponent";
    expandedDiv.id="expandedComponent" + components.length;
    collapsedDiv.appendChild(expandedDiv);
    //add the details
    jQuery(expandedDiv).html(htmlComponentDetails);

    //clear the fields
    jQuery('#addComponentForm').trigger("reset");
    //set today's date for receivedDate
    jQuery("#receivedDate").datepicker('setDate', new Date());
    //hide the kit info
    jQuery('#kitInfo').hide();
  };

  function saveKitComponents(){
    Fluxion.doAjax(
      'kitComponentControllerHelperService',
      'saveKitComponents',
      {
        'components':components,
        'url': ajaxurl
      },
      {'doOnSuccess': function () {
        alert("The component(s) have been successfully logged");
        location.reload(true); //reload the page from the server
      }
    });
  };

  function getKitInfoByReferenceNumber(){
    var referenceNumber = jQuery("#referenceNumber").val();

    Fluxion.doAjax(
      'kitComponentControllerHelperService',
      'getKitInfoByReferenceNumber',
      {
        'referenceNumber':referenceNumber,
        'url': ajaxurl
      },
      {'doOnSuccess': function (json) {
        if(jQuery.isEmptyObject(json)){
          alert("The reference number is not recognised");
        }else{
          var htmlStrResult = "<h2>Kit Info </h2><br><b>Name:</b> \t" + json.name + "<br>" +
                  "<b>Component:</b> \t" + json.componentName + "<br>" +
                  "<b>Version:</b> \t" + json.version + "<br>" +
                  "<b>Manufacturer:</b> \t" + json.manufacturer + "<br>" +
                  "<b>Part Number:</b> \t" + json.partNumber + "<br>" +
                  "<b>Type:</b> \t" + json.kitType + "<br>" +
                  "<b>Platform:</b> \t" + json.platformType + "<br>" +
                  "<b>Units:</b> \t" + json.units + "<br>" +
                  "<b>Value: </b>\t" + json.kitValue + "<br><br>";

          currentComponentFullName = json.name + " " + json.componentName;
          jQuery('#kitInfo').html(htmlStrResult);
          jQuery('#kitInfo').show();
        }
      }
    });
  };
</script>
</div>
</div>

<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>
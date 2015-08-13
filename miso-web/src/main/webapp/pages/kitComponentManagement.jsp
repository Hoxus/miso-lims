<%@ include file="../header.jsp" %>

<%--
  ~ Copyright (c) 2015. The Genome Analysis Centre, Norwich, UK
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
        <h1>Kit Component Management</h1>

        <div id="identificationBarcodeForm">
            Scan the identification barcode and press Enter
            <input type="text" name="identificationBarcode" id="identificationBarcode"/>
        </div>

        <div id="kitInfo" style="display: none">
            <h2>Kit Info</h2>
            <table id="descriptorInfoKitTable" class="in">
                <tr>
                    <td class="h">Descriptor Name:</td>
                    <td id="name"></td>
                </tr>
                <tr>
                    <td class="h">Component Name:</td>
                    <td id="componentName"></td>
                </tr>
                <tr>
                    <td class="h">Reference Number:</td>
                    <td id="referenceNumber"></td>
                </tr>
                <tr>
                    <td class="h">Lot Number:</td>
                    <td id="lotNumber"></td>
                </tr>
                <tr>
                    <td class="h">Received Date:</td>
                    <td id="receivedDate"></td>
                </tr>
                <tr>
                    <td class="h">Expiry Date:</td>
                    <td id="expiryDate"></td>
                </tr>
                <tr>
                    <td class="h">Location Barcode:</td>
                    <td id="locationBarcode"></td>
                </tr>
                <tr>
                    <td class="h">Exhausted:</td>
                    <td id="exhausted"></td>
                </tr>
            </table>
            <br>
            <br>
        </div>

        <div id="changeLogTableDiv" style='display:none'>
            <h2>Change log</h2>
            <table id="changeLogTable" cellpadding="0" cellspacing="0" border="0"  class="display"></table>
        </div>

        <div id="actions" style='display:none'>
            <br>
            <h2>Actions</h2>
            <select id="selectAction">
                <option selected="selected" value='-1'>Choose what you want to do with this component</option>
                <option value='0'>Exhaust</option>
                <option value='1'>Change location</option>
            </select>

        </div>

        <div id="locationBarcodeForm" style="display:none">
            <table class="in">
                <tr>
                    <td class="h">New Location:</td>
                    <td> <input type="text" name="locationBarcode" id="newLocationBarcode"/></td>
                </tr>
            </table>
        </div>

        <div id="exhaustDiv" style="display:none">
            Reason:
            <form id="exhaustChangeLocation">
                <input type="radio" name="changeLocation" value='false' checked="checked">Used up (no new location required)<br>
                <input type="radio" name="changeLocation" value='true'>Other (specify new location)
            </form>

            <table class="in">
                <tr>
                    <td class="h">Are you sure you want to exhaust this kit?</td>
                    <td> <button type="button" id="exhaustComponentButton">Exhaust</button></td>
                </tr>
            </table>
        </div>

        <div id="changeLocationDiv" style="display:none">
            <table class="in">
                <tr>
                    <td class="h"> Are you sure you want to change location of this kit?</td>
                    <td> <button type="button" id="changeLocationButton">Change location</button></td>
                </tr>
            </table>
        </div>


    </div>
</div>


<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/r/dt/dt-1.10.8,se-1.0.0/datatables.min.css"/>
<script type="text/javascript" src="https://cdn.datatables.net/r/dt/dt-1.10.8,se-1.0.0/datatables.min.js"></script>
<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js"></script>
<script type="text/javascript" src="http://cdn.datatables.net/plug-ins/1.10.9/sorting/datetime-moment.js"></script>

<script>
    var changeLog;
    var identificationBarcode;
    var locationBarcodeOld;
    var locationRequired = false;
    var exhausted = false;

    //trigger:  page loaded
    //action:   focus on identificationBarcode field
    //          prepareChangeLogTable() - no data
    jQuery(document).ready(function(){
        jQuery("#identificationBarcode").focus();
        prepareChangeLogTable(changeLog);

    });

    //trigger:  press enter on start page
    //action:   getKitInfoByIdentificationNumber()
    jQuery("#identificationBarcode").keypress(function(e){
        if(e.which==13){
            identificationBarcode = jQuery(this).val();
            getKitInfoByIdentificationNumber();
        }

    });

    //trigger:  choose from dropdown list of actions (exhaust/changeLocation)
    //action:   show exhaust divs / show changeLocation divs
    jQuery("#selectAction").change(function(){
        var selected = jQuery("#selectAction").val();

        //0-exhaust
        //1-change location


        if(exhausted && (selected==0 || selected==1)){
            alert("This kit component has already been exhausted");
        }else {

            switch (selected) {
                case '0':
                    jQuery("#exhaustDiv").show();
                    jQuery("#changeLocationDiv").hide();
                    jQuery("#locationBarcodeForm").hide();
                    locationRequired = false;
                    break;
                case '1':
                    jQuery("#exhaustDiv").hide();
                    jQuery("#locationBarcodeForm").show();
                    jQuery("#changeLocationDiv").show();
                    locationRequired = true;
                    break;
                default:
                    jQuery("#exhaustDiv").hide();
                    jQuery("#locationBarcodeForm").hide();
                    jQuery("#changeLocationDiv").hide();
                    locationRequired = false;
            }
        }
    })

    //trigger:  click on radio button in exhaustChangeLocation
    //action:   depending on choice - show/hide locationBarcodeForm
    jQuery("#exhaustChangeLocation").change(function(){
        var changeLocation = jQuery('input[name=changeLocation]:checked', '#exhaustChangeLocation').val();

        if (changeLocation =='true'){
            locationRequired = true;
            jQuery("#locationBarcodeForm").show();
        }else{
            locationRequired = false;
            jQuery("#locationBarcodeForm").hide();
        }
    })

    //trigger:  click on exhaustComponentButton
    //action:   exhaustKitComponent()
    jQuery("#exhaustComponentButton").click(function() {
                if (locationRequired && !isLocationFilledIn()) {
                    alert("New location barcode is required")
                } else {
                    exhaustKitComponent();
                }
            });

    //trigger:  click on changeLocationButton
    //action:   changeKitLocation()
    jQuery("#changeLocationButton").click(function(){
        if (locationRequired && !isLocationFilledIn()) {
            alert("New location barcode is required")
        } else {
            changeKitLocation();
        }
    });

    //description:  checks if newLocationBarcode is filled in
    function isLocationFilledIn(){
        var value=jQuery.trim(jQuery("#newLocationBarcode").val());

        if(!(value.length>0))
        {
            return false;
        }

        return true;
    }

    //description:  exhausts this kit component (ajax call)
    //feedback:     alert, reload page
    function exhaustKitComponent(){

        var locationBarcodeNew;

        if(locationRequired){
            locationBarcodeNew = jQuery("#newLocationBarcode").val();
        }else{
            locationBarcodeNew='';
        }

        Fluxion.doAjax(
                'kitComponentControllerHelperService',
                'exhaustKitComponent',

                {
                    'identificationBarcode':identificationBarcode,
                    'locationBarcodeNew': locationBarcodeNew,
                    'locationBarcodeOld': locationBarcodeOld,
                    'url': ajaxurl
                },
                {
                    'doOnSuccess': function (json) {

                        alert("The kit component has been successfully exhausted");
                        location.reload(true);
                    }
                });
    }

    //description:  change this kit's location (ajax call)
    //feedback:     alert, reload page
    function changeKitLocation(){
        var locationBarcodeNew = jQuery("#newLocationBarcode").val();


        Fluxion.doAjax(
                'kitComponentControllerHelperService',
                'changeLocation',

                {
                    'identificationBarcode':identificationBarcode,
                    'locationBarcodeNew': locationBarcodeNew,
                    'locationBarcodeOld': locationBarcodeOld,
                    'url': ajaxurl
                },
                {
                    'doOnSuccess': function (json) {

                        alert("The kit component has been successfuly relocated");
                        location.reload(true);
                    }
                });
    }

    //description:  get kit info by identification number (ajax call)
    //feedback:     alert if identification barcode is not recognised
    //action:       showKitInfo(), listChangeLogForTable()
    //display:      actions div
    function getKitInfoByIdentificationNumber(){
        Fluxion.doAjax(
                'kitComponentControllerHelperService',
                'getKitInfoByIdentificationBarcode',

                {
                    'identificationBarcode':identificationBarcode,
                    'url': ajaxurl
                },
                {'doOnSuccess': function (json) {


                    if (jQuery.isEmptyObject(json)) {
                        alert("The identification barcode is not recognised");

                    } else {
                        exhausted = json.exhausted;
                        showKitInfo(json);
                        jQuery("#actions").show();
                        locationBarcodeOld = json.locationBarcode;
                        listChangeLogForTable(json.kitComponentId);
                    }

                }
                });
    }

    //description:  display kit info
    //display:      kitInfo div
    //hide:         identificationBarcodeForm
    function showKitInfo(json){
        Object.keys(json).forEach(function(key){
            jQuery("#"+key).html(json[key]);
        })

        jQuery("#exhausted").html(exhausted.toString());
        jQuery("#kitInfo").show();
        jQuery("#identificationBarcodeForm").hide();

    }

    //description:  get change log for this kit component (ajax call)
    //action:       update table
    //display:      changeLogTableDiv
    function listChangeLogForTable(kitComponentId){

        Fluxion.doAjax(
                'kitComponentControllerHelperService',
                'getKitChangeLogByKitComponentId',

                {
                    'kitComponentId': kitComponentId,
                    'url': ajaxurl

                },
                {'doOnSuccess': function (json) {


                    changeLog = json.changeLog;
                    table.clear();
                    table.rows.add(changeLog).draw();
                    jQuery('#changeLogTableDiv').show();
                }

                });
    };

    //description:  prepare change log table (first initialisation - no data)
    function prepareChangeLogTable(changeLog){

        jQuery.fn.dataTable.moment('DD-MM-YYYY HH:mm:SS');  //enables proper sorting

        table = jQuery("#changeLogTable").DataTable({
            data: changeLog,
            columns:[
                { data: 'userId', title: "User ID"},
                { data: 'exhausted', title: "Exhausted"},
                { data: 'locationBarcodeOld', title: "Location Barcode Before Change"},
                { data: 'locationBarcodeNew', title: "Location Barcode After Change"},
                { data: 'logDate', title: "Date of Change"}
            ],
            paging: false,
            order: [[ 4, "desc" ]]

        })};

</script>
<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>
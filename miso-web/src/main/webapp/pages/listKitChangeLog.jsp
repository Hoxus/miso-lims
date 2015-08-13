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

<%@ include file="../header.jsp" %>

<div id="maincontent">
  <div id="contentcolumn">

    <h1>Kit Change Log</h1>

    <div id="changeLogTableDiv">
      <table id="changeLogTable" cellpadding="0" cellspacing="0" border="0"  class="display"></table>
    </div>

  </div>
</div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.css"/>

<script type="text/javascript" src="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.js"></script>
<script type="text/javascript" src="http://jquery-datatables-column-filter.googlecode.com/svn/trunk/media/js/jquery.dataTables.columnFilter.js"></script>
<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js"></script>
<script type="text/javascript" src="http://cdn.datatables.net/plug-ins/1.10.9/sorting/datetime-moment.js"></script>

<script>
  var changeLog;
  var table;

  //trigger:  page load
  //action:   listChangeLogForTable()
  jQuery(document).ready(function(){
    listChangeLogForTable();
  });

  //description:  get change log
  //feedback:     on success - prepareChangeLogTable()
  function listChangeLogForTable(){
    Fluxion.doAjax(
            'kitComponentControllerHelperService',
            'getKitChangeLog',

            {
              'url': ajaxurl
            },
            {'doOnSuccess': function (json) {

              changeLog = json.changeLog;

              prepareChangeLogTable(changeLog);

            }

            });
  };

  //description:  prepare change log table
  function prepareChangeLogTable(changeLog){

    jQuery.fn.dataTable.moment('DD-MM-YYYY HH:mm:SS');

    table = jQuery("#changeLogTable").DataTable({
      data: changeLog,
      columns:[
        { data: 'userId', title: "User ID"},
        { data: 'kitComponentId', title: "Kit Component ID"},
        { data: 'exhausted', title: "Exhausted"},
        { data: 'locationBarcodeOld', title: "Previous Location Barcode"},
        { data: 'locationBarcodeNew', title: "Location Barcode"},
        { data: 'logDate', title: "Date of Change"}
      ],
      paging: false,
      order: [[ 5, "desc" ]]
    })};

</script>

<%@ include file="adminsub.jsp" %>
<%@ include file="../footer.jsp" %>
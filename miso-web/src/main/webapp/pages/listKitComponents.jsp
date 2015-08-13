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

    <h1>Kits List</h1>
    <h2>Visibility Options</h2><button type="button" id="showVisibilityOptions">Toggle Visibility Options</button>
    <br>
    <div id="visibilityOptions" style="display:none">
      <form name="showMore" id="showMoreForm">

        <table>
          <tr>
            <th>
              Exhausted:
            </th>
          </tr>
          <tr>
            <td>
              <button type="button" id='exhaustedVisibilityButton'>Show/Hide</button>
            </td>
          </tr>
          <tr>
            <th>
              Show only:
            </th>
          </tr>
          <tr>
            <td>
              <select id="selectExpiryDisplay">
                <option selected="selected" value='-1'>Show all</option>
                <option value='0'>Expired</option>
                <option value='1'>Soon to expire</option>
              </select>
            </td>
          </tr>
          <tr>
            <th>Search by ID:</th>
            <td>
          </tr>
          <tr>
            <td>
              <input type="number" id="searchId">
            </td>
            <td>
              <button type="button" id='searchIdButton'>Find</button>
            </td>
          </tr>
          <tr>
            <th>Show additional fields:</th>
          </tr>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="3"> Version
            </td>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="5"> Part Number
            </td>
          </tr>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="6"> Type
            </td>
          </tr>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="7"> Platform
            </td>
          </tr>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="8"> Units
            </td>
          </tr>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="9"> Value
            </td>
          </tr>
          <tr>
            <td>
              <input type="checkbox" name="showMore" value="10"> Reference Number
            </td>
          </tr>
        </table>
      </form>
    </div>

    <div id="kitsTableDiv">

      <table id="kitsTable" cellpadding="0" cellspacing="0" border="0"  class="display"></table>
    </div>

  </div>
</div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.css"/>

<script type="text/javascript" src="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.js"></script>
<script type="text/javascript" src="http://jquery-datatables-column-filter.googlecode.com/svn/trunk/media/js/jquery.dataTables.columnFilter.js"></script>



<script>
  var components;
  var table;
  var exhaustedToggled = true;


  //trigger:  page load
  //action:   listAllKitComponentsForTable()
  jQuery(document).ready(function(){
    listAllKitComponentsForTable();
  });

  //trigger:  click on showVisibilityOptions button
  //action:   toggle visibilityOptions div
  jQuery("#showVisibilityOptions").click(function(){
    jQuery("#visibilityOptions").slideToggle();
  })

  //trigger:  click on searchIdButton ("Find")
  //action:   search table for this id
  jQuery("#searchIdButton").click(function(){
    var id = jQuery("#searchId").val();
    table.column(0).search(id).draw(); //0 is index od ID column
  })

  //trigger:  check/uncheck boxes in "Show additional fields"
  //action:   add/remove additional fields to/from table
  jQuery("input[type=checkbox]").click(function(){
    jQuery('input[type=checkbox]').each(function(){
      if(this.checked){
        table.column(jQuery(this).val()).visible(true);
      }else{
        table.column(jQuery(this).val()).visible(false);
      }
    })
  })

  //trigger:  click on exhaustedVisibilityButton
  //action:   toggleVisibilityExhausted()
  jQuery("#exhaustedVisibilityButton").click(function(){
    toggleVisibilityExhausted();
  });

  //trigger:  change on selectExpiryDisplay ("Exhausted: Show/Hide")
  //action:   filterByExpiry()
  jQuery("#selectExpiryDisplay").change(function(){
    filterByExpiry();
  })

  //description:  look at hidden fields in table and sort table
  //              based on expiry status
  //action:       update table, redraw
  function filterByExpiry(){
    var selected = jQuery("#selectExpiryDisplay").val();
    var expiryStateColumn = 17; //index (by default that columns is hidden)

    switch(selected){

      case "-1":
        //show all
        table.column(expiryStateColumn).search("").draw();
        break;
      case "0":
        //show only expired
        table.column(expiryStateColumn).search("0").draw();
        break;
      case "1":
        //show only soon-to-expire
        table.column(expiryStateColumn).search("1").draw();
        break;
    }


  }

  //description:  show/hide exhausted components
  //action:       update table, redraw
  function toggleVisibilityExhausted(){
    if(exhaustedToggled){ //16 -index of exhausted column
      table.column(16).search("false").draw();
      exhaustedToggled = false;
    }else{
      table.column(16).search("").draw();
      exhaustedToggled = true;
    }
  }

  //description:  get list of all kit components formatted for table (ajax call)
  //feedback:     prepareTable(), toggleVisibilityExhausted() - hide exhausted by default
  function listAllKitComponentsForTable(){
    Fluxion.doAjax(
            'kitComponentControllerHelperService',
            'listAllKitComponentsForTable',

            {
              'url': ajaxurl
            },
            {'doOnSuccess': function (json) {

              components = json.components;

              prepareTable(json.components);
              toggleVisibilityExhausted(); //by default hide exhausted

            }

            });
  };

  //description:  prepare table with all kit components details
  function prepareTable(components){
    table = jQuery("#kitsTable").DataTable({

      data: components,
      columns:[
        { data: 'ID', title: "ID"},
        { data: 'Kit Name', title: "Kit Name"},
        { data: 'Component Name', title: "Component Name"},
        { data: 'Version', title: "Version", visible:false},
        { data: 'Manufacturer', title: "Manufacturer"},
        { data: 'Part Number', title: "Part Number", visible:false},
        { data: 'Type', title: "Type", visible:false},
        { data: 'Platform', title: "Platform", visible: false},
        { data: 'Units', title: "Units", visible:false},
        { data: 'Value', title: "Value", visible:false},
        { data: 'Reference Number', title: "Reference Number", visible:false},
        { data: 'Identification Barcode', title: "Identification Barcode"},
        { data: 'Lot Number', title: "Lot Number"},
        { data: 'Location Barcode', title: "Location Barcode"},
        { data: 'Received Date', title: "Received Date"},
        { data: 'Expiry Date', title: "Expiry Date"},
        { data: 'Exhausted', title: "Exhausted"},
        { data: 'Expiry State', title: "Expiry State", visible:false}

      ],
      paging: false,
      order: [[ 15, "desc" ]],
      initComplete: function () {
        this.api().columns().every( function () {
          var column = this;
          var select = jQuery('<select><option value=""></option></select>')
                  .appendTo( jQuery(column.header()) )
                  .on( 'change', function () {
                    var val = jQuery.fn.dataTable.util.escapeRegex(
                            jQuery(this).val()
                    );

                    column.search( val ? '^'+val+'$' : '', true, false ).draw();
                  } );

          column.data().unique().sort().each( function ( d, j ) {
            select.append( '<option value="'+d+'">'+d+'</option>' )
          } );
        } );
      }



    })};

</script>

<%@ include file="adminsub.jsp" %>
<%@ include file="../footer.jsp" %>
<%--
  Created by IntelliJ IDEA.
  User: zakm
  Date: 26/08/2015
  Time: 12:52
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="../header.jsp" %>




<div id="maincontent">
  <div id="contentcolumn">

    <h1>Kit Descriptor Management</h1>

    <div id="descriptorDiv">

      <div id="selectKitDescriptorDiv">
        <table id="selectKitDescriptorTable" class="in">
          <tr>
            <td class="h">Select a Kit Descriptor: </td>
            <td>
              <select id="selectKitDescriptorDropdown">
              </select>
            </td>
          </tr>
        </table>
      </div>
      <br>
      <div id="descriptorInfoDiv" style="display:none">
        <div id="descriptorInfoKit">
          <h2>Kit Descriptor Info</h2>
          <table id="descriptorInfoKitTable" class="in">
            <tr>
              <td class="h">ID:</td>
              <td id="kitDescriptorId"></td>
            </tr>
            <tr>
              <td class="h">Name:</td>
              <td id="name"></td>
            </tr>
            <tr>
              <td class="h">Version:</td>
              <td id="version"></td>
            </tr>
            <tr>
              <td class="h">Manufacturer:</td>
              <td id="manufacturer"></td>
            </tr>
            <tr>
              <td class="h">Part Number:</td>
              <td id="partNumber"></td>
            </tr>
            <tr>
              <td class="h">Type:</td>
              <td id="kitType"></td>
            </tr>
            <tr>
              <td class="h">Platform:</td>
              <td id="platformType"></td>
            </tr>
            <tr>
              <td class="h">Units:</td>
              <td id="units"></td>
            </tr>
            <tr>
              <td class="h">Value:</td>
              <td id="kitValue"></td>
            </tr>

          </table>
        </div>

        <div id="descriptorInfoComponents" style="display:none">
          <br>
          <h2>Components Descriptors</h2>
          <table id="descriptorInfoComponentsTable">
          </table>
        </div>

      </div>




    </div>

  </div>
</div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/r/dt/dt-1.10.8,se-1.0.0/datatables.min.css"/>

<script type="text/javascript" src="https://cdn.datatables.net/r/dt/dt-1.10.8,se-1.0.0/datatables.min.js"></script>

<script>
  var descriptors;
  var descriptor;
  var componentDescriptors;
  var componentDescriptorsTable;

  //trigger:  page loaded
  //action:   listAllKitDescriptors()
  jQuery(document).ready(function(){
    listAllKitDescriptors();
    prepareTable(componentDescriptors);
  });


  //trigger: change selection on the drop down list of Kit Descriptors
  //action:  chooseKitDescriptor() 
  //          and showKitDescriptorInfo() / hideKitDescriptorInfo() 
  jQuery("#selectKitDescriptorDropdown").change(function(){
    var index = jQuery("#selectKitDescriptorDropdown").val();

    if(index>=0){ //"Select a kit descriptor" default option has index -1
      chooseKitDescriptor(index);
      showKitDescriptorInfo(descriptor)
    }
    else{
      hideKitDescriptorInfo();
    }
  })

  //description: hide kit descriptor info div
  function hideKitDescriptorInfo(){
    jQuery("#descriptorInfoDiv").hide();
  }

  //description: show kit descriptor info div (populate kit descriptors table)
  //             list kit component descriptors
  function showKitDescriptorInfo(descriptor){
    Object.keys(descriptor).forEach(function(key){

      jQuery("#"+key).html(descriptor[key]);
    })
    listKitComponentDescriptorsByKitDescriptorId(descriptor.kitDescriptorId);
    jQuery("#descriptorInfoDiv").show();

  }

  //choose the kit descriptor whose info will be displayed
  function chooseKitDescriptor(index){

    descriptor = descriptors[index];

  }


  //description: get a list of all kit descriptors 
  //            and populateKitDescriptorDropdownList() 
  function listAllKitDescriptors(){

    Fluxion.doAjax(
            'kitComponentControllerHelperService',
            'listAllKitDescriptors',

            {
              'url': ajaxurl
            },
            {'doOnSuccess': function (json) {

              descriptors = json.descriptors;
              populateKitDescriptorDropdownList(descriptors);


            }
            });
  }

  //description: populate kit descriptor dropdown list
  function populateKitDescriptorDropdownList(descriptors){
    var dropdown = jQuery("#selectKitDescriptorDropdown");

    dropdown.append(jQuery("<option />").val(-1).text("Select a kit descriptor"));
    var index = 0;
    jQuery.each(descriptors,function(){
      dropdown.append(jQuery("<option />").val(index).text(this.name));
      index++;
    });
  }


  //description: get a list of kit components descriptors by provided kit descriptor ID 
  //             and showKitComponentDescriptorInfo()
  function listKitComponentDescriptorsByKitDescriptorId(kitDescriptorId){
    Fluxion.doAjax(
            'kitComponentControllerHelperService',
            'listKitComponentDescriptorsByKitDescriptorId',

            {
              'kitDescriptorId':kitDescriptorId,
              'url': ajaxurl
            },
            {'doOnSuccess': function (json) {

              componentDescriptors = json.componentDescriptors;

              showKitComponentDescriptorInfo();
            }
            });
  }

  function showKitComponentDescriptorInfo(){

    jQuery("#descriptorInfoComponents").show();
    componentDescriptorsTable.clear();
    componentDescriptorsTable.rows.add(componentDescriptors).draw();

  }

  function prepareTable(componentDescriptors){
    componentDescriptorsTable = jQuery("#descriptorInfoComponentsTable").DataTable({

      data: componentDescriptors,
      columns:[
        { data: 'kitComponentDescriptorId', title: "ID"},
        { data: 'name', title: "Component Name"},
        { data: 'referenceNumber', title: "Reference Number"},
        { data: 'stockLevel', title: "Stock Level"}
      ],
      paging: false,
    });
  }





</script>


<%@ include file="adminsub.jsp" %>
<%@ include file="../footer.jsp" %>
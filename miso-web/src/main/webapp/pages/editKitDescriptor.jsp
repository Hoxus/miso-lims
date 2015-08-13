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
        <form:form action="/miso/kitdescriptor" method="POST" commandName="kitDescriptor" autocomplete="off" id="addDescriptorForm">

            <sessionConversation:insertSessionConversationId attributeName="kitDescriptor"/>

            <h1>New Kit
            </h1>
            <h2>Information</h2>
            <table class="in">
                <tr>
                    <td class="h">Name:</td>
                    <td><form:input path="name" id="name"/></td>
                </tr>
                <tr>
                    <td class="h">Version:</td>
                    <td><form:input path="version"/></td>
                </tr>
                <tr>
                    <td class="h">Manufacturer:</td>
                    <td><form:input path="manufacturer"/></td>
                </tr>
                <tr>
                    <td class="h">Part Number:</td>
                    <td><form:input path="partNumber" id="partNumber"/></td>
                </tr>
                <tr>
                    <td class="h">Units:</td>
                    <td><form:input path="units"/></td>
                </tr>
                <tr>
                    <td class="h">Value:</td>
                    <td><form:input path="kitValue"/></td>
                </tr>
                <tr>
                    <td>Type:</td>
                    <td>
                        <form:select id="kitTypes" path="kitType" items="${kitTypes}"/>
                    </td>
                </tr>
                <tr>
                    <td>Platform:</td>
                    <td>
                        <form:select id="platformTypes" path="platformType" items="${platformTypes}"/>
                    </td>
                </tr>
            </table>

            <div id="fillTheForm"><br><i>To continue you have to fill out all the fields.</i></div>
            <button type="submit" class="fg-button ui-state-default ui-corner-all" id="submit" style="display:none">Save and proceed to add components
            </button>
        </form:form>


    </div>
</div>

<script>

    //trigger:  keyup on addDescriptorForm
    //action:   check if fields have been filled in
    //feedback: show submit button on success
    jQuery("#addDescriptorForm").keyup(function(){
        var empty= jQuery(this).find("input").filter(function(){
            return this.value === "";
        });

        if(!(empty.length)){
            jQuery("#submit").show();
            jQuery("#fillTheForm").hide();
        }else{
            jQuery("#submit").hide();
            jQuery("#fillTheForm").show();
        }

    });

    //trigger:  page load
    //action:   focus on name field
    jQuery(document).ready(function(){
        jQuery("#name").focus();
    })

    //trigger:  keyup on partNumber
    //action:   isPartNumberAlreadyInDB()
    //feedback: alert and clear field if true
    jQuery("#partNumber").keyup(function(){
        isPartNumberAlreadyInDB(jQuery(this).val());


    })

    //description:  check if there is kit descriptor with this part number in db
    //action:       return true/false accordingly
    function isPartNumberAlreadyInDB(partNumber){

        Fluxion.doAjax(
                'kitComponentControllerHelperService',
                'getKitDescriptorByPartNumber',

                {
                    'partNumber': partNumber,
                    'url': ajaxurl

                },
                {'doOnSuccess': function (json) {

                    if(!jQuery.isEmptyObject(json)){
                        alert("This part number is already registered in the database");
                        jQuery('#partNumber').val('');
                    }
                }

                });
    };
</script>

<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>
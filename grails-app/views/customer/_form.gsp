<%@ page import="etsycustomer.Customer" %>



<div class="fieldcontain ${hasErrors(bean: customerInstance, field: 'userID', 'error')} required">
	<label for="userID">
		<g:message code="customer.userID.label" default="User ID" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="userID" type="number" value="${customerInstance.userID}" required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: customerInstance, field: 'userName', 'error')} required">
	<label for="userName">
		<g:message code="customer.userName.label" default="User Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="userName" required="" value="${customerInstance?.userName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: customerInstance, field: 'email', 'error')} required">
	<label for="email">
		<g:message code="customer.email.label" default="Email" />
		<span class="required-indicator">*</span>
	</label>
	<g:field type="email" name="email" required="" value="${customerInstance?.email}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: customerInstance, field: 'referredByUser', 'error')} ">
	<label for="referredByUser">
		<g:message code="customer.referredByUser.label" default="Referred By User" />
		
	</label>
	<g:field name="referredByUser" type="number" value="${customerInstance.referredByUser}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: customerInstance, field: 'addresses', 'error')} ">
	<label for="addresses">
		<g:message code="customer.addresses.label" default="Addresses" />
		
	</label>
	<g:select name="addresses" from="${etsycustomer.Address.list()}" multiple="multiple" optionKey="id" size="5" value="${customerInstance?.addresses*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: customerInstance, field: 'transactions', 'error')} ">
	<label for="transactions">
		<g:message code="customer.transactions.label" default="Transactions" />
		
	</label>
	<g:select name="transactions" from="${etsycustomer.Transaction.list()}" multiple="multiple" optionKey="id" size="5" value="${customerInstance?.transactions*.id}" class="many-to-many"/>
</div>


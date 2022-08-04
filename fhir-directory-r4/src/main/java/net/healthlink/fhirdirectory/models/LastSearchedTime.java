/*
 * Copyright (c) 2020 HealthLink Limited.
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 * 
 * @author Sajith Jamal
 */

package net.healthlink.fhirdirectory.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LastSearchedTime {
	private Date lastSearchedDate;
	
	public LastSearchedTime()
	{
		
	}
	
	

	public LastSearchedTime(Date lastSearchedDate) {
		super();
		this.lastSearchedDate = lastSearchedDate;
	}

	@Id
	@Column
	public Date getLastSearchedDate() {
		return lastSearchedDate;
	}

	public void setLastSearchedDate(Date lastSearchedDate) {
		this.lastSearchedDate = lastSearchedDate;
	}
	
	
}

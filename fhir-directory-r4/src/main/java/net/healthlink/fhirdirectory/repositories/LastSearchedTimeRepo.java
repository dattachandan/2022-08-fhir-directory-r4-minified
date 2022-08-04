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

package net.healthlink.fhirdirectory.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.healthlink.fhirdirectory.models.LastSearchedTime;

@Repository
public interface LastSearchedTimeRepo extends JpaRepository<LastSearchedTime, String> {
	
	LastSearchedTime findFirstByOrderByLastSearchedDateDesc();

}

package com.nexus.lancorC.back.appartmentez.repository

import com.nexus.lancorC.back.appartmentez.entity.LoginHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoginHistoryRepository : JpaRepository<LoginHistory, Long>
package com.rrsgroup.customer.service.message

import com.rrsgroup.customer.domain.message.MessageStatus
import com.rrsgroup.customer.entity.message.Message
import com.rrsgroup.customer.repository.MessageRepository
import com.rrsgroup.customer.service.EventService
import spock.lang.Specification

import java.time.LocalDateTime

class MessageServiceSpec extends Specification {

    MessageRepository messageRepository = Mock()
    EventService eventService = Mock()
    MessageService messageService = new MessageService(messageRepository, eventService)

    def "saveMessageAnonymous should set audit fields and save message"() {
        given:
        def message = new Message()

        when:
        def result = messageService.saveMessageAnonymous(message)

        then:
        1 * messageRepository.save(_ as Message) >> { args -> args[0] }
        result.status == MessageStatus.UNREAD
        result.createdBy == "anonymous"
        result.updatedBy == "anonymous"
        result.createdDate != null
        result.updatedDate != null
    }

    def "saveMessageAnonymous should set current timestamps"() {
        given:
        def message = new Message()
        messageRepository.save(_ as Message) >> { args -> args[0] }

        when:
        def result = messageService.saveMessageAnonymous(message)

        then:
        def now = LocalDateTime.now()
        result.createdDate.isBefore(now) || result.createdDate.isEqual(now)
        result.updatedDate.isBefore(now) || result.updatedDate.isEqual(now)
    }

    def "saveMessageAnonymous should return message returned by repository"() {
        given:
        def input = new Message()
        def persisted = new Message(id: 123L)
        messageRepository.save(_ as Message) >> persisted

        when:
        def result = messageService.saveMessageAnonymous(input)

        then:
        result.is(persisted)
    }
}

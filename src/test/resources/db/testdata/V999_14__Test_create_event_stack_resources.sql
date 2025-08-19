insert into event_stack_resource (id, dead_letter_queue_1_name, dead_letter_queue_2_name, mailbox_queue_1_name,
                                  mailbox_queue_2_name, event_stack_policy_document_name, stack_id, creation_timestamp,
                                  env_id, app_env_depl_id)
VALUES ('other_poja_application_event_1_resource_id', 'deadQueue1', null, 'mailboxQueue1', null, null,
        'event_stack_1_id', '2023-07-18T10:15:30.00Z', 'other_poja_application_environment_id', 'deployment_1_id'),
       ('other_poja_application_env2_event_1_resource_id', 'deadQueue1Env2', 'deadQueue2Env2', 'mailboxQueue1Env2',
        'other_poja_application_environment_2_id', null, 'event_stack_env_2_1_id', '2024-08-02 14:30:00',
        'other_poja_application_environment_2_id', 'deployment_2_id'),
       ('other_poja_application_event_2_resource_id', 'deadQueue1', 'deadQueue2', 'mailboxQueue1', 'mailboxQueue2',
        null, 'event_stack_1_id', '2024-08-07 12:15:00', 'other_poja_application_environment_id', 'deployment_12_id'),
       ('other_poja_application_event_3_resource_id', 'deadQueue1', null, 'mailboxQueue1', null, null,
        'event_stack_1_id', '2024-08-08 12:30:00', 'other_poja_application_environment_id', 'deployment_7_id');

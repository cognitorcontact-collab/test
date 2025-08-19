# [14.5.0](https://github.com/poja-app/poja-api/compare/v14.4.0...v14.5.0) (2025-07-30)


### Bug Fixes

* events are not sent to sentry ([24f3938](https://github.com/poja-app/poja-api/commit/24f393838f1ac7f4946cb2ec7953762796477d58))
* updateStackOrCreateIfNotExistsOnCloud ([09e39f7](https://github.com/poja-app/poja-api/commit/09e39f7645abba420556b4854fb39190203064c9))


### Features

* suspension grace period ([d379071](https://github.com/poja-app/poja-api/commit/d37907135fae59f344b98066701fda23456f40dc))



# [14.4.0](https://github.com/poja-app/poja-api/compare/v14.3.0...v14.4.0) (2025-07-17)


### Bug Fixes

* activate user if no reason to suspend ([ff1fcec](https://github.com/poja-app/poja-api/commit/ff1fcecd929c4e3226882ad691f142e6de6065b8))
* cast to pojaConf3 if version is pojaConf3 ([ae3ab76](https://github.com/poja-app/poja-api/commit/ae3ab765d38c47ed42d1a5824b23be73c0e2b249))
* duplicate app name ([05c5f6e](https://github.com/poja-app/poja-api/commit/05c5f6e5a452a7618bc3a8219c62e56d5e2253fa))
* prefix app name with user id ([e20a22b](https://github.com/poja-app/poja-api/commit/e20a22ba8498da3fd455e39b849e9bebdaccfbd6))


### Features

* user billing discount ([43742bf](https://github.com/poja-app/poja-api/commit/43742bfdee6c6c5f0ed39c0f412a0512c8ecca14))



# [14.3.0](https://github.com/poja-app/poja-api/compare/v14.2.0...v14.3.0) (2025-06-13)


### Features

* usersBilling sorts ([c16c9b7](https://github.com/poja-app/poja-api/commit/c16c9b7dc9491e90b0f36bd8a332c9868bf1d3ba))



# [14.2.0](https://github.com/poja-app/poja-api/compare/v14.1.1...v14.2.0) (2025-06-06)


### Features

* add UserStatistics::(suspended_users_nb, archived_users_nb) ([b0a0b56](https://github.com/poja-app/poja-api/commit/b0a0b56768dcdbf7150b3d63305416fa16a86e70))


### Performance Improvements

* rm useless filter on securityConf ([a272da9](https://github.com/poja-app/poja-api/commit/a272da952b1d3476d93d393b191bb107430590ec))



## [14.1.1](https://github.com/poja-app/poja-api/compare/v14.1.0...v14.1.1) (2025-06-04)


### Bug Fixes

* new component for poja3, cannot map multiple versions to a same component even with different discriminator value ([35c2cc6](https://github.com/poja-app/poja-api/commit/35c2cc64a10c92d15c106fae961e358804fdb5aa))



# [14.1.0](https://github.com/poja-app/poja-api/compare/v14.0.0...v14.1.0) (2025-06-04)


### Features

* integrate new poja3 same as poja2 but patched ([1ee9086](https://github.com/poja-app/poja-api/commit/1ee9086fe8314747ad4634d08717e451e8b973cb))



# [14.0.0](https://github.com/poja-app/poja-api/compare/2ba66c0d68c22d3c31d4102c6fd43f0e4b42e522...v14.0.0) (2025-06-04)


### Bug Fixes

* add user to console group on invite acceptation ([ba36ee9](https://github.com/poja-app/poja-api/commit/ba36ee90d48c1798d117ff7295376a61410db0db))
* add user to org console group only after user upserted ([bbd6a8e](https://github.com/poja-app/poja-api/commit/bbd6a8ede1597c856ce7cd4e776515d7bd6e6ff7))
* added check for empty result in get billing info query result ([8476e7d](https://github.com/poja-app/poja-api/commit/8476e7d88d0156c4f1e623a884c22b34d6747e3d))
* api url to poja.io api url ([c6d0b4e](https://github.com/poja-app/poja-api/commit/c6d0b4e29b7e469a4959c1e6714facf58441d2b2))
* app env deployment is null or manual redeployment ([eb9900a](https://github.com/poja-app/poja-api/commit/eb9900ac428b03dcb885a931e5b4e7bafc0edb67))
* billing info compute datetime is null ([57c47b8](https://github.com/poja-app/poja-api/commit/57c47b88d2a49cbfe6bdc20d66704237998153a6))
* billing info not updated after calculation ([cf5bdac](https://github.com/poja-app/poja-api/commit/cf5bdac64eac607c57b2e043c117fa1b5909cd58))
* bucket key path does not starts with a slash ([755e817](https://github.com/poja-app/poja-api/commit/755e8177ab5c3cc102f69da030051c8b60d5ea64))
* can create new environment after archiving one ([e2459cf](https://github.com/poja-app/poja-api/commit/e2459cfd51128cea570249b0775fb52c6d152d4b))
* cancel modifying no more existant postgresql column ([8238146](https://github.com/poja-app/poja-api/commit/8238146825719db855af668b44ac31fb4d9fa91e))
* cannot create different applications with the same name ([c3efbab](https://github.com/poja-app/poja-api/commit/c3efbab754124422c58384090fb0b7c460b8ca5d))
* check stmt.actions if both stmts sid are null ([4b13214](https://github.com/poja-app/poja-api/commit/4b13214e32a7ca15bfa5341df1b438137c7f64d5))
* check user github id by token instead of email ([7751c5b](https://github.com/poja-app/poja-api/commit/7751c5b51373b7fba2d06d7c40d3005825c96546))
* circular dependency on saving appEnvDeployment and DeploymentState ([bf8c748](https://github.com/poja-app/poja-api/commit/bf8c74876e192d1e64e1b5e873921896c58a88c3))
* ComputeStackCrupdateTriggeredServiceIT ([8d2908c](https://github.com/poja-app/poja-api/commit/8d2908c45b6b87c739094274703abc0ab1c2d3d7))
* count user groups by org and user returns null ([19a7a44](https://github.com/poja-app/poja-api/commit/19a7a4490923904823b90cfae141012b2c4680fd))
* create log file in temp directory ([0cc9893](https://github.com/poja-app/poja-api/commit/0cc9893e24c3d3e53117cc6827e77607d596f2e8))
* create separate migration to update invoice status enum ([64c98d0](https://github.com/poja-app/poja-api/commit/64c98d08fe4b37c5807049384bab6d6153c9ac30))
* crupdate compute stack outputs after stack deployment succeded ([f0fd8c8](https://github.com/poja-app/poja-api/commit/f0fd8c84a4f6dfab3a8210c69765f7df2a208808))
* crupdate policy statments and sync if possible ([c78f2fe](https://github.com/poja-app/poja-api/commit/c78f2fe71c9a435d62d8e76c40ea8c3d4825f8ee))
* crupdate stack events ([c855ba3](https://github.com/poja-app/poja-api/commit/c855ba3c87d8957d782f9b97fa42ae586dfe134f))
* define policy document name on org upsertion ([4d4222d](https://github.com/poja-app/poja-api/commit/4d4222dc1d3fc714d7835ac4994606ac8d425f76))
* destination filename for cd-compute ([d7bd301](https://github.com/poja-app/poja-api/commit/d7bd301d211b670aabaad6de4caddf40c4fa5854))
* detach payment method ([ce763b9](https://github.com/poja-app/poja-api/commit/ce763b9e75e25179f0f0ecff1b4447245fca7d52))
* do not check compute stack status on stacks deployments ([738c02f](https://github.com/poja-app/poja-api/commit/738c02facaf11ca57a9de043771a0e470abf5335))
* do not deleted alias referenced lambda versions ([6f5f828](https://github.com/poja-app/poja-api/commit/6f5f8287dbb58f388761a6cd39be4b32911ef32f))
* do not remove reconfigured cd-compute from code to be pushed ([4bccc55](https://github.com/poja-app/poja-api/commit/4bccc558225bb4897fb38dd368860755d9c9ddee))
* do not save event stack file if there's not and save sqlite stack file to s3 ([a16123c](https://github.com/poja-app/poja-api/commit/a16123c0594d34809b22105bfa063caf99fb50f8))
* do not throw NotImplementedException on trying to retrieve resources for not handled stack types ([c354890](https://github.com/poja-app/poja-api/commit/c3548902fef6c3599028055988f2788f563024f6))
* duplicated scheduler name ([4476cdc](https://github.com/poja-app/poja-api/commit/4476cdcd2b506b95ada89d66ea4323ade050dfcb))
* env deploy tests ([9a16c81](https://github.com/poja-app/poja-api/commit/9a16c81bb2035305688d514995c1379faa9890a7))
* env deployment config is null ([a4885a7](https://github.com/poja-app/poja-api/commit/a4885a752fad73e650b36ba6b82d78d5bfdec287))
* environment variable is case sensitive ([5ad9467](https://github.com/poja-app/poja-api/commit/5ad946700b86b875b17cc7cb9be3791c918b2015))
* exception thrown on access denied is from spring security ([0f959d6](https://github.com/poja-app/poja-api/commit/0f959d65a1d30e52252ca78c31c3410ccafe21e3))
* forgotten params on create log query ([34a66e5](https://github.com/poja-app/poja-api/commit/34a66e54d1fdb04b32b6e3c4ebccbf8e228dafdb))
* get deployment list ([e46ce84](https://github.com/poja-app/poja-api/commit/e46ce84d701dbfda94859f36e5dfe998566ff1e6))
* get new saved app env id on handling user deployment commit ([aa6644f](https://github.com/poja-app/poja-api/commit/aa6644f78a6dfb4bcf54536b50becced3274cffb))
* getUserBillingInfo throws NoSuchElementException ([6d97439](https://github.com/poja-app/poja-api/commit/6d97439bf276c3e1df8c02233ee2b0363762b135))
* handle error thrown when compute stack is not existing yet ([07a7019](https://github.com/poja-app/poja-api/commit/07a7019f383fc6d751fd8ec2823f7d871e8569c6))
* handle null user group on stack permission removal ([a37e617](https://github.com/poja-app/poja-api/commit/a37e617cbcd566fd463e53a28fd296ccb2277023))
* handle null values on function names ([b6b0b08](https://github.com/poja-app/poja-api/commit/b6b0b08f4d3e494d3e4ba584d47e2cb877ee24c4))
* handle payment intent succeeded status ([cdc9f96](https://github.com/poja-app/poja-api/commit/cdc9f965c4ddb1c651598dc3c2ea15688e06b536))
* handle POJA_2 conf upload ([5b5db0e](https://github.com/poja-app/poja-api/commit/5b5db0ee5bd4886fe3d8e0710e62bc5f24ab21f6))
* ignore getEventStack in CheckTemplateIntegrityTriggered ([b241f0c](https://github.com/poja-app/poja-api/commit/b241f0c124107b360d6e13247a1ea66835a42897))
* independant stack deployment skipped ([8d60e92](https://github.com/poja-app/poja-api/commit/8d60e92f414da4cf0048d460832575db79dda8e5))
* independent stack deployement state fails if only one of them fails ([f120979](https://github.com/poja-app/poja-api/commit/f12097907891275e7aab1aa1896437346fa903b4))
* lambdaUrl & httpApi -> lambda-url & http-api ([eb61041](https://github.com/poja-app/poja-api/commit/eb61041dbd59c92396f7c32c96c9ab0fe94d27a9))
* LinkedHashMap type in stackDatas ([0ce42ae](https://github.com/poja-app/poja-api/commit/0ce42ae15b4c292ce1e3aa7461a3f825533dc231))
* make billing info update transactional ([fed173b](https://github.com/poja-app/poja-api/commit/fed173b6e5c3506c9383e21d8ba9a12ba15fb3c0))
* map lambda dashboard function url when retrieve with /compute_stack_resources ([9e12920](https://github.com/poja-app/poja-api/commit/9e12920c67a5bcbbb2f34d9e028887e392dbcb97))
* member can remove themeself from org ([3220ccb](https://github.com/poja-app/poja-api/commit/3220ccbbb114d4e50bd8137fba892246d7235eaa))
* migration ([aea7a46](https://github.com/poja-app/poja-api/commit/aea7a46e5aa2da01cc1823751a44987114353cc1))
* missing env var ([9bc347a](https://github.com/poja-app/poja-api/commit/9bc347a870c7f7acbc8c64a202d22f90d9c684a2))
* new spec for read app env deployments and test data changes ([1e6d942](https://github.com/poja-app/poja-api/commit/1e6d942843be1d64eaa25cc56496e9d7ac78f822))
* no new app env deployment is created when template file check is initiated ([4fab89d](https://github.com/poja-app/poja-api/commit/4fab89ddfdf4d4ae380bab419fcb787c65ecc09b))
* not invited users not included in org invitees suggestions ([c8a22a5](https://github.com/poja-app/poja-api/commit/c8a22a5c370b4031e4b633b584d2d0d3b8824f52))
* only newly created orgs are upserted ([72270ea](https://github.com/poja-app/poja-api/commit/72270ea13df6201ab5a01781cad58fd8ebe933f0))
* only one prod and preprod env by application can be created ([925224e](https://github.com/poja-app/poja-api/commit/925224e49584c08842f55d6b62965e86df7aec5f))
* org members count computation ([d35d3e8](https://github.com/poja-app/poja-api/commit/d35d3e85d97cd376da4cd288839263799133f16c))
* org_id -> user_id ([00aa29e](https://github.com/poja-app/poja-api/commit/00aa29e4d179abf5fe2fe66dc5cf5801b0686a1a))
* **poja-conf:** poja custom are HashMaps ([73cd8a3](https://github.com/poja-app/poja-api/commit/73cd8a36ad9527f330ffcd0b80489e8a96559103))
* pojaConf snapstart is boolean ([1d55fed](https://github.com/poja-app/poja-api/commit/1d55fedcb937637f0e70b9f775769c8c7b94c419))
* **policy:** stmt sid is possibly null ([9eb94f2](https://github.com/poja-app/poja-api/commit/9eb94f21de530980d7d6ade611d6c9756b1a947b))
* presign tag headers for temp files ([4a2e000](https://github.com/poja-app/poja-api/commit/4a2e00053349ab025ca5d4d61eda5ea0faf080b1))
* read deployment states IT tests and reformat code ([c3a40bb](https://github.com/poja-app/poja-api/commit/c3a40bba64581438877c5c28463f3c4c817d7381))
* reference log group name as query parameter instead of path variable ([bb93323](https://github.com/poja-app/poja-api/commit/bb9332380f8ecc6c5355cb24ab3914c6e60c8752))
* refine policy update logic for group statements ([ff444d9](https://github.com/poja-app/poja-api/commit/ff444d944cac5288c6e7531bcf1f03dbfafebd72))
* remove duplicated stack id ([5618141](https://github.com/poja-app/poja-api/commit/561814123d2530148dfeffb72b4fa2cc6f85f27d))
* remove unused endpoints ([9fc39c7](https://github.com/poja-app/poja-api/commit/9fc39c745319c6fe3433dfa95564b6c618173ad0))
* remove unused total memory used ([ddaa0d6](https://github.com/poja-app/poja-api/commit/ddaa0d6bf59f8ffe5501b4fe963e7997eacaffa0))
* rename build_template_file_url to build_template_file_uri ([e9f44cc](https://github.com/poja-app/poja-api/commit/e9f44cc98b46c10774d72dfa36a27b34869ca25d))
* rename user role attributea and default value ([45552a1](https://github.com/poja-app/poja-api/commit/45552a1ebd05f88ddf3086f9244dd5e3158e705b))
* replace old stack type EVENT_1 and EVENT_2 by only EVENT ([def3e95](https://github.com/poja-app/poja-api/commit/def3e95bf23a7161ddc96a45763e74b449b56b36))
* retrieve payment details ([5682086](https://github.com/poja-app/poja-api/commit/5682086cc0e1ceef5938ae7558c525ce8cddf749))
* return an empty list when there's no ssm param yet ([e181a41](https://github.com/poja-app/poja-api/commit/e181a413c0550787458a39754015a6dc67aefda3))
* return empty list if there's not stack events either stack outputs ([562f795](https://github.com/poja-app/poja-api/commit/562f795fa87adad185b1dd31ae0c62ea29285d70))
* rm previous handler if poja_2 update ([f83a54a](https://github.com/poja-app/poja-api/commit/f83a54a80998eac621d4e32e006eaf37ffd6c299))
* security conf ([99629bf](https://github.com/poja-app/poja-api/commit/99629bf703cf36318918b3a35581430c2fd5699d))
* separate endpoint to create and update ssm parameters ([2798bb0](https://github.com/poja-app/poja-api/commit/2798bb0641ff89d389d426ecd45a311df2b68aae))
* set github state to failed ([1e4c363](https://github.com/poja-app/poja-api/commit/1e4c36394db3e6bcbd8d189aacb2da11e44ee99b))
* stack appname is not anymore given as parameter ([ae80005](https://github.com/poja-app/poja-api/commit/ae8000598c81468962fddb44a608d87284aca31c))
* stack data pagination ([d62a852](https://github.com/poja-app/poja-api/commit/d62a85238be93c165f31d051836fe7048d5f5ac7))
* stack events and stack outputs pagination ([1108f68](https://github.com/poja-app/poja-api/commit/1108f6846c2f8506bc0c6cd35a855daa659ea2f9))
* **temporary:** return a zero billing info when none is found within time range ([1d3b9f7](https://github.com/poja-app/poja-api/commit/1d3b9f79163f28db1d6f2050002b6d79beaf733f))
* **test:** configure mocks ([535fa61](https://github.com/poja-app/poja-api/commit/535fa61dc1c6197e9a082ac954f1313e62af9c6c))
* **test:** reference app deployment id when setting github workflow state to failed ([360429a](https://github.com/poja-app/poja-api/commit/360429ab5d650d3389d2783c31ec8074cc4e81e2))
* **tests:** correctly map nullable values for poja conf ([f37f583](https://github.com/poja-app/poja-api/commit/f37f583f2127efd03a692da70a8c69b1c5c0e539))
* unique github id per user ([7eabc78](https://github.com/poja-app/poja-api/commit/7eabc788f118e9e95bbe0d52db4c03579093195e))
* update deployer event source pattern ([681a96e](https://github.com/poja-app/poja-api/commit/681a96ed9bc34bb57e657581db0258cbf820da0a))
* update jcloudify deployer event source pattern ([48a330f](https://github.com/poja-app/poja-api/commit/48a330f1e823ce3bb5d45720fdafbef1c7e6a446))
* update progression_status type values ([fd4bf70](https://github.com/poja-app/poja-api/commit/fd4bf7023e592a8c796abc1b7fc7e1246c4c3810))
* update stack when it already exists ([bda3bf6](https://github.com/poja-app/poja-api/commit/bda3bf6892a699f3789d7c608fd06ab8b1d29b7b))
* user active_sub+latest_sub, suspension_duration computation ([b1d33a7](https://github.com/poja-app/poja-api/commit/b1d33a7154f723d1d5c883bb8efc79c5360f365e))
* users are not billed if they joined at earlier year ([ca8ef31](https://github.com/poja-app/poja-api/commit/ca8ef3117c5a7cbeb938447a12f48a90934ea95b))
* wrong exception caught during authentication ([ae73a60](https://github.com/poja-app/poja-api/commit/ae73a60a4830754d1de3a5321928d15077b39b29))
* yearmonth billing ([d29e50a](https://github.com/poja-app/poja-api/commit/d29e50a4906508b54bb7f1ce8285aeecb4cb0f79))


### chore

* set code version to 2.0.0 ([51ae182](https://github.com/poja-app/poja-api/commit/51ae182a71d23c29e4fd4fdb009b103823725abd))


* feat!: general.with_queues_nb -> compute.with_queues_nb ([4f4cc01](https://github.com/poja-app/poja-api/commit/4f4cc01ade661bd6fd4c4a17e647fb19e5da423e))
* feat!: new poja conf with separated worker confs, and scheduler stack ([0d7b9f8](https://github.com/poja-app/poja-api/commit/0d7b9f806b912f7861b01d29a8d80ee8805a38f7))
* feat!: create iam per org ([3c0c89d](https://github.com/poja-app/poja-api/commit/3c0c89da48f9fa658386e09bf47665a03ec17df6))
* fix!: /users/stats and /users/{uid} is ambiguous ([e418a77](https://github.com/poja-app/poja-api/commit/e418a773caad1a477f33820d0f580e46a9c594c3))
* feat!: get resources billing info by org ([64f37f3](https://github.com/poja-app/poja-api/commit/64f37f370dad33c60ceab9ce51aef8c02fab056d))
* feat!: read & write monitoring resources, env resources, log queries by org id ([e0b9ad9](https://github.com/poja-app/poja-api/commit/e0b9ad9c4fec83c2f827305ddd9a92dce509bb9a))
* feat!: read & write configs, deployments, app installations, stacks by org id ([64124cf](https://github.com/poja-app/poja-api/commit/64124cf02ee802f49e76237d9e2f3ac63f44dfff))
* feat!: read & write app env with org_id ([b6611b1](https://github.com/poja-app/poja-api/commit/b6611b1f3d937a052afc8178bf999e1ce320a614))
* feat!: different models for BillingInfos by user, app and env ([4aaeade](https://github.com/poja-app/poja-api/commit/4aaeade64d478cf53804ea89af3cd1497e969509))


### Features

* add ?username filter on GET usersBilling ([990c74a](https://github.com/poja-app/poja-api/commit/990c74ab701cd7eb2c967368c9855ec82d19dd89))
* add avatar url and type to app installation, also rename app installation to a more precise GithubAppInstallation ([c1d4ab3](https://github.com/poja-app/poja-api/commit/c1d4ab34ca2fdd83209e1cde196ed2494bd8bbbc))
* add conf id to depl ([9c9d31d](https://github.com/poja-app/poja-api/commit/9c9d31daffcb80e02990fb3bc9cdca25e812fa6e))
* add frontal_function_invocation_method to PojaConf2 ([87d1f27](https://github.com/poja-app/poja-api/commit/87d1f2709e4432669cb888480541a9c9d90a299a))
* add GetUserResponse::archived ([53135ab](https://github.com/poja-app/poja-api/commit/53135abcb79d91cb2e89fa8536aa46c919ed047f))
* add GetUserResponse::status ([f1c131e](https://github.com/poja-app/poja-api/commit/f1c131e27823457aa7217e67c11f540fb17c1125))
* add GithubRepository is empty ([e146fc4](https://github.com/poja-app/poja-api/commit/e146fc4018a77b16b215009f71107a3c1439f5bc))
* add humanReadableVersion to PojaVersion in order to have the same string representation over api and consumers ([d919513](https://github.com/poja-app/poja-api/commit/d919513b777b4a77090ea4d7c342884e14294eb2))
* add imported attribute to distinguish imported repos from non-imported ones ([54ce759](https://github.com/poja-app/poja-api/commit/54ce7599a1b5ab0babb3eff8348582595a2904f1))
* add latest deployed url to env ([c13a488](https://github.com/poja-app/poja-api/commit/c13a4882c972df68a044c63266fff634f7549214))
* add repoId to Application.GithubRepository ([a14e050](https://github.com/poja-app/poja-api/commit/a14e050db9ae8d1f859c37a422bc7f677ebfb0ab))
* add repository description to application, and fix poja-conf tests ([28b8a46](https://github.com/poja-app/poja-api/commit/28b8a463c0264aae19b0d72dd7d678409e55c31d))
* add repository html url to app if imported ([f8cf16a](https://github.com/poja-app/poja-api/commit/f8cf16a00519a0304bb3299e4047598707c6e5e8))
* add users threshold validator and get user stats ([643a98b](https://github.com/poja-app/poja-api/commit/643a98bc2f35a1b3b1ed217e7b3da1381c545b51))
* add year to payment request ([37203ff](https://github.com/poja-app/poja-api/commit/37203ff46ef5cdbd22c92292e5ec7a881222c857))
* add/remove org member ([bd99079](https://github.com/poja-app/poja-api/commit/bd990798a622eb13ec8b70b1c1fb33839dbe11af))
* app installations ([79dafa8](https://github.com/poja-app/poja-api/commit/79dafa8e553f8db6c12862779749316f4600e9d5))
* archive stacks ([c8205cc](https://github.com/poja-app/poja-api/commit/c8205ccc7af8ae823a6e044ff7d5abb72557365e))
* archive, suspend/activate users ([0f5b499](https://github.com/poja-app/poja-api/commit/0f5b499f4fe997d7b0b6893cb5336486d51aebd7))
* async console user creation ([0a9acd3](https://github.com/poja-app/poja-api/commit/0a9acd3e2a924234d280ad9f33cbb7e9a2b50518))
* authenticate either with github user token or app token ([e25dc17](https://github.com/poja-app/poja-api/commit/e25dc17c170a490e08c87da662491409ae39b413))
* avatar field to user data ([596a495](https://github.com/poja-app/poja-api/commit/596a4956bd0b4ce5a8391228c6597933be5b220d))
* beta-ping is the first beta-user-only endpoint ([43b5bef](https://github.com/poja-app/poja-api/commit/43b5bef84bb1f58d91162bb8e5c63e25bf0e2e96))
* billing info by user for admin ([fad7778](https://github.com/poja-app/poja-api/commit/fad7778ecc9d4439b6218c2b4cd1d3b9a4c75a93))
* bootstrap jcloudify api project ([2ba66c0](https://github.com/poja-app/poja-api/commit/2ba66c0d68c22d3c31d4102c6fd43f0e4b42e522))
* can crupdate applications at PUT /applications ([1ff2575](https://github.com/poja-app/poja-api/commit/1ff2575f40bdc329ef22993847ae7a4db326f08f))
* cancel org user invitation ([6eda43b](https://github.com/poja-app/poja-api/commit/6eda43b1feedf6d5dc4aae779a97e4d0789dd940))
* cancel queued deployments and deploy latest (no cloudformation) ([771cf39](https://github.com/poja-app/poja-api/commit/771cf390fb5cc5f1952ab2cbf4246128ea8b584f))
* canceling deployments, not deployed yet deployments can reach state canceling and canceling state can become anything ([090eaa7](https://github.com/poja-app/poja-api/commit/090eaa7194452d4eadb1692b08cec003bc58fe46))
* change custom_java_deps and custom_java_repositories from string map to string list ([3512a62](https://github.com/poja-app/poja-api/commit/3512a625ae501630ca508848a2bea4e43369f991))
* check built template before processing to deployment ([e764127](https://github.com/poja-app/poja-api/commit/e76412759fdd63cec79656e499aae49a61ad67e6))
* configure application environment ([3e47e95](https://github.com/poja-app/poja-api/commit/3e47e9571e1175def69d0ef6bafe2b6925be4d9e))
* configure github authentication ([7d9519c](https://github.com/poja-app/poja-api/commit/7d9519c58844e615b9149a3ed8e3e97e076378cf))
* configure state machine ([54d3503](https://github.com/poja-app/poja-api/commit/54d3503d88df220496ca5c113bc6065fd5f70bb2))
* consumer provided id for envconf ([18b52da](https://github.com/poja-app/poja-api/commit/18b52dab9bdd0cd3372eaf699050d0485598e8fb))
* create and list environments, created environments have UNKNOWN state by default ([fa337e4](https://github.com/poja-app/poja-api/commit/fa337e44778195606b3bc4fd3bf117812d60cd80))
* create and read log query, api docs only, alr implemented ([bbc2219](https://github.com/poja-app/poja-api/commit/bbc221961bcf332ce572cdd96c9cbf94625c8af8))
* create console user group on org creation ([a14219a](https://github.com/poja-app/poja-api/commit/a14219ae3ba4671184c616b35260ec4f8a31760d))
* create console user sync with User.hasConsoleCredentials flag ([0e065a1](https://github.com/poja-app/poja-api/commit/0e065a101636fe7ee81471136cf427dbb218735a))
* create pending depl on env config and return deplId ([2299e7b](https://github.com/poja-app/poja-api/commit/2299e7b74dd6547783987ade11a8a481732fbbe9))
* create user main org on signup ([2c811a3](https://github.com/poja-app/poja-api/commit/2c811a3261b29ca4bac45eb12ca950c77acdd7cf))
* crupdate all log streams ([11d4203](https://github.com/poja-app/poja-api/commit/11d42038781948f98651d869abdf91051ada2872))
* crupdate github app installation by user ([5bc187d](https://github.com/poja-app/poja-api/commit/5bc187d20863a9420caf4b6004a53436a81c7fc9))
* crupdate github workflow state ([9afe6e5](https://github.com/poja-app/poja-api/commit/9afe6e576aa5ef5b1f7d863d9af73d932c0259d5))
* crupdate log streams ([15c1949](https://github.com/poja-app/poja-api/commit/15c194961d3953ff8f80e57ad6ce7c3bdfc9d547))
* crupdate orgs ([91069d6](https://github.com/poja-app/poja-api/commit/91069d6a93d7477e442e6aff7c724d54a492312d))
* crupdate repo on application crupdate ([c9276f7](https://github.com/poja-app/poja-api/commit/c9276f7b28fc57cb7c67e1168f63bd1ee69daca3))
* crupdate ssm parameters ([de4ff05](https://github.com/poja-app/poja-api/commit/de4ff0529dddac330b691488c64c23678c13e947))
* deactivate mailer ([fd088c5](https://github.com/poja-app/poja-api/commit/fd088c52fd6a086398b7b787346f84fa118a2318))
* delete cloudformation stack by name ([4a8a87d](https://github.com/poja-app/poja-api/commit/4a8a87d7069cc7c6052637044422664da47f9f0b))
* deploy needed stacks before compute stack ([a723796](https://github.com/poja-app/poja-api/commit/a723796d7d6c7cf469fe11eff19d1d45a3f0f31f))
* deployments with workflow uri ([434aec3](https://github.com/poja-app/poja-api/commit/434aec346190e3e76cdca8b3f07f1ebd2ae527aa))
* discount amount on payments ([54a09d7](https://github.com/poja-app/poja-api/commit/54a09d7e81ed65384486280fe9a8e77f3c447737))
* env resources by deployment ([fc9070b](https://github.com/poja-app/poja-api/commit/fc9070b3ed6be12a50e488b82f324f2a3c4ac0bb))
* EnvironmentResources.hasResources ([fa11e2a](https://github.com/poja-app/poja-api/commit/fa11e2a35c8bcf7f2bb863d2e8bc152e2dd084d8))
* filter logquery by envTypes and by functionTypes ([49dc082](https://github.com/poja-app/poja-api/commit/49dc0822e74bbcbefcf7c37b1c991d9c8c6e7778))
* filter stack-events by from to ([358b705](https://github.com/poja-app/poja-api/commit/358b705303f8388b85a0b76cac28215155a83c3c))
* get & update user org invites ([3f4e459](https://github.com/poja-app/poja-api/commit/3f4e4595edc782ab48c259ed1f9fc80b0642a39c))
* get all applications filtered by name and userId ([04b42fa](https://github.com/poja-app/poja-api/commit/04b42fa63dbda469ef05243a222aeb8e53f52320))
* get app env deployment config ([01002f8](https://github.com/poja-app/poja-api/commit/01002f81587880157f5c6efe8f682d58fb34e580))
* get app installation repos by user id ([dd1cc5b](https://github.com/poja-app/poja-api/commit/dd1cc5b58cfc789e5bff01715d1e74f4375559c4))
* get app installations by user ([f0ee0a8](https://github.com/poja-app/poja-api/commit/f0ee0a8df071a93c0e6b0b077e959a20ca08bc14))
* get application by id ([11363d8](https://github.com/poja-app/poja-api/commit/11363d8c2ac0a3c0edb5a257a6957c9c5a383305))
* get cloudwatch log groups ([0a06c1c](https://github.com/poja-app/poja-api/commit/0a06c1ca00f0da5699e948b160db865571829b31))
* get deployment progression list ([19a3e4e](https://github.com/poja-app/poja-api/commit/19a3e4e3f36c69aec673dc4ab9392a5b5af6a744))
* get deployments and get deployment ([71d9354](https://github.com/poja-app/poja-api/commit/71d93542f3ebab6145ccf812ed3d4bb9e7c8abe4))
* get envionment by id ([e0d69ba](https://github.com/poja-app/poja-api/commit/e0d69bad398ebe788b8541434e24afd0f206ae81))
* get log events of a log stream (not implemented) ([c32d387](https://github.com/poja-app/poja-api/commit/c32d387f5ba5d8806fd20e0cd474711a651a0d3a))
* get log group from stack by function name ([1752543](https://github.com/poja-app/poja-api/commit/1752543e6429be297242253cac46429a5ea89a08))
* get log stream events ([1a5a076](https://github.com/poja-app/poja-api/commit/1a5a076c9b0599be812247406c2dcc19f63b11a2))
* get log streams ([53b9eb5](https://github.com/poja-app/poja-api/commit/53b9eb576c18dbfd4fbff3be12680f28901d8547))
* get org by id ([81cb6fb](https://github.com/poja-app/poja-api/commit/81cb6fb54c51246548f0bf57e21e2e10176c5dea))
* get org invitees suggestions with username filter ([cc91d8a](https://github.com/poja-app/poja-api/commit/cc91d8a9b9b669c5c4dfbd0d68a02e5fbab2da9e))
* get org invites by status ([107a00e](https://github.com/poja-app/poja-api/commit/107a00e11577f5b8aa283dcfe3ad4ac03b139391))
* get org members ([9e01a05](https://github.com/poja-app/poja-api/commit/9e01a05fc53098a0ee9eb81035e39e45fc227d8e))
* get paginated users with username filter ([09bca85](https://github.com/poja-app/poja-api/commit/09bca8505d5f010f5f80f4f178e1e3a7d9eeeaf0))
* get ssm parameters ([514a589](https://github.com/poja-app/poja-api/commit/514a589b2b9143356dd6e8d95277906c7fb7e024))
* get stack by id ([7834ded](https://github.com/poja-app/poja-api/commit/7834ded5c984df186fc45aa55c304b1bb29dedb1))
* get stack events ([b50655a](https://github.com/poja-app/poja-api/commit/b50655a18b519d084a568988d58906b50cddf803))
* get stack outputs ([e36d54d](https://github.com/poja-app/poja-api/commit/e36d54d846aef02b810851e8b751e0025b0c2344))
* get stack resources ([9a2da1f](https://github.com/poja-app/poja-api/commit/9a2da1fea2957fde0065bea9c0a6dfe1f35dad6c))
* get user billing info per application ([4b59650](https://github.com/poja-app/poja-api/commit/4b596509ec466f9fbc81b5d9a632c6b3dd18c28e))
* get user billing info per environment ([829c7c0](https://github.com/poja-app/poja-api/commit/829c7c05b57988f9d82fc14b2871c0b0f83f1f96))
* get user by id ([5ac052d](https://github.com/poja-app/poja-api/commit/5ac052dcec5edf01f897fd631dc55f395a048965))
* get user orgs ([2df2e9d](https://github.com/poja-app/poja-api/commit/2df2e9dd4346f86d97b391aaf9f5e65c002bff33))
* get user total billing info ([f636ac8](https://github.com/poja-app/poja-api/commit/f636ac809971f22b0e8dd8ccf2f03a78aff31cfc))
* getUsersBilling ?archived filter ([fa7ede0](https://github.com/poja-app/poja-api/commit/fa7ede02b5a9fd231d32279d74c288b3b0142181))
* gh workflow run attempt on depl ([dc37b3c](https://github.com/poja-app/poja-api/commit/dc37b3c11fab06b32ac004c29a7a4c3c1c5a5604))
* github repository name and scope(private or public) ([f5b86db](https://github.com/poja-app/poja-api/commit/f5b86db8c88b5c45b13a422daa9e578f62d513f4))
* GithubRepositoryListItem has default branch attribute ([e7b8429](https://github.com/poja-app/poja-api/commit/e7b8429c38a5f6527e08a105452a8557b20b8e29))
* handle archiving applications ([3f4cf72](https://github.com/poja-app/poja-api/commit/3f4cf7201403aa1ebe678a28f60ea28fb5c64d67))
* handle s3 putobject presigned url ([197623d](https://github.com/poja-app/poja-api/commit/197623dd36e45b3bdbcd5b01f6758a44794f44cd))
* if active==null return all subs of user order by s of user order by sub.subBeginDatetimeDesc ([7c9d2a9](https://github.com/poja-app/poja-api/commit/7c9d2a99fd19e4ca05c952571cb4e47b8e98ce4c))
* implement endpoint to exchange github code into token ([5436877](https://github.com/poja-app/poja-api/commit/543687714d676d72d73cb8a4225380e1ce4b9616))
* individually deploy event stack 1 and 2 ([6501251](https://github.com/poja-app/poja-api/commit/6501251c98c79265f17c257f9ba441cd3fb8128b))
* initiate cloudformation stack deployments ([1d851c8](https://github.com/poja-app/poja-api/commit/1d851c8a1c4a3071e8bba1a9ed22009484d0c6ed))
* initiate stack deployment ([c1f66ab](https://github.com/poja-app/poja-api/commit/c1f66ab47262147e8a240f07c3af094c91227a5a))
* larger datatype for user appcount ([59c87f1](https://github.com/poja-app/poja-api/commit/59c87f1cd2f4be8d8be4d798ddddc4749576c178))
* link appEnvDeployment with computeStackResource ([d47044f](https://github.com/poja-app/poja-api/commit/d47044f1a146c452b76e5663b6bf7d01815db97a))
* list poja-versions read from resources/files/poja_versions.json ([ee6d7f5](https://github.com/poja-app/poja-api/commit/ee6d7f5ab1d5b739424f0f5d763e3bcbcf880784))
* list repositories ([16d9621](https://github.com/poja-app/poja-api/commit/16d962140081c61e65de4e1853d4ebd8df6cee10))
* log queries ([3a098c0](https://github.com/poja-app/poja-api/commit/3a098c018e99e0ee2d527da283c7deaed9e9fa97))
* log queries ([c1801c3](https://github.com/poja-app/poja-api/commit/c1801c3251cccc69c2319390856f83b81f8b745a))
* manage payment method ([3ddba4b](https://github.com/poja-app/poja-api/commit/3ddba4b617217d6966016c10116b8ee50d6ede64))
* manually suspend/activate app/env ([dd7c851](https://github.com/poja-app/poja-api/commit/dd7c8513d759be4b2ddd5f835585b08a84c92488))
* monthly payment attempts and manual payments ([55d0dec](https://github.com/poja-app/poja-api/commit/55d0dec83b879ed0a13df87de02a66d3e0cb8e62))
* monthType on billing endpoint ([604c781](https://github.com/poja-app/poja-api/commit/604c781a8820d068c84cf7f7c42cdd7691931295))
* move stack log to env log ([494e621](https://github.com/poja-app/poja-api/commit/494e6214f4a1aba0ab767a6551dc880268fa4542))
* new user_status, new env_status UNDER_MODIFICATION ([ed08774](https://github.com/poja-app/poja-api/commit/ed08774d771d31d43369d9c68fbfa26c96611ab9))
* **no-implementation:** make some poja conf nullable ([cb0fbf0](https://github.com/poja-app/poja-api/commit/cb0fbf0e87ec4f34f080528d3d23640c28beceb8))
* **not-implemented:** env resources grouped by deployment ([1c53979](https://github.com/poja-app/poja-api/commit/1c539792b2f7ab92c46feed68943d2b7036417fd))
* only start deployment after template file check ([76e732d](https://github.com/poja-app/poja-api/commit/76e732d5095d7914f3577757ffe8e85a451a3e2b))
* org billing read ([0e50f73](https://github.com/poja-app/poja-api/commit/0e50f739a3c3b4d21149cff86a45ff47c8fe5a91))
* orginvite.invited_at ([8cf7823](https://github.com/poja-app/poja-api/commit/8cf7823720edc95bd917b2194801cd11e328765d))
* paginated log query ([f1f5a9b](https://github.com/poja-app/poja-api/commit/f1f5a9bde6def2d119370c746a3a654b51a1bf08))
* payment customer ([9e782ac](https://github.com/poja-app/poja-api/commit/9e782acaaf27018fbfc89fa73e20cd0a54e56dd5))
* periodically crupdate log stream events ([efbe28f](https://github.com/poja-app/poja-api/commit/efbe28fec364c3bb630e97c4d3e9fdf430ec89af))
* poja cli integration with git repo push ([c22b8ba](https://github.com/poja-app/poja-api/commit/c22b8ba58d1534698d88e9e8ebbc274341e8a729))
* poja version is hidden as internal, shown version is jcloudifys and will be mapped internally ([c8739ff](https://github.com/poja-app/poja-api/commit/c8739ff196468ee50f07f60f2e0ca63e0c85dacc))
* PojaConfV17_0_0 ([d29fd7b](https://github.com/poja-app/poja-api/commit/d29fd7b7c3b5b5d8e3da50cbad18729bd9b5905b))
* pre-save tag after workflow run ([e9fbbe2](https://github.com/poja-app/poja-api/commit/e9fbbe2c2a564c1ebb8f7c6bbcd06954c32a6841))
* pricing is directly attached to user, defaulting to TEN_MICRO, there is no other ref ([301a12e](https://github.com/poja-app/poja-api/commit/301a12e5c4ffe04235744484d017a06045406420))
* public generator 1.2.0 ([597e713](https://github.com/poja-app/poja-api/commit/597e713723d6830e1b081297c95bb69287990f65))
* public generator version 1.1.0 ([9fcb32a](https://github.com/poja-app/poja-api/commit/9fcb32a61f60182c7e287dd243b648088c981a2c))
* queued deployment with indep queued state ([929d38e](https://github.com/poja-app/poja-api/commit/929d38e32f970b1f138bc375e3765bd4d8875dff))
* re-use FunctionMonitoringResource component ([ff5f431](https://github.com/poja-app/poja-api/commit/ff5f43148b67a20f6bd7bd56f269d3877fbcc340))
* read & write applications by org_id ([5a1f5a2](https://github.com/poja-app/poja-api/commit/5a1f5a293618f65f901bd5175a20684326f7ca37))
* redeploy envs ([f2250f5](https://github.com/poja-app/poja-api/commit/f2250f54bcdad530280860b1d555d0d47e098d90))
* refresh github access token ([13b0337](https://github.com/poja-app/poja-api/commit/13b033751fb952170f00cab906673f6647418469))
* remove SQLite support for stacks ([cf3af34](https://github.com/poja-app/poja-api/commit/cf3af34ccd4afc301e6ea7e23951d0856ed10b83))
* rename project from JCloudify Api to Poja Api ([77f7a13](https://github.com/poja-app/poja-api/commit/77f7a134abee26f5f05f864ac19c53a86b6a957d))
* reset generator public version to 1.0.0 ([7664d47](https://github.com/poja-app/poja-api/commit/7664d47e7b1122bc5d8cf035ce8562b77d8b7e7e))
* return compute stack creation datetime ([9153ebc](https://github.com/poja-app/poja-api/commit/9153ebc4a698910453a526a9f9a6f69f582d437f))
* return refresh token with access token ([3caf931](https://github.com/poja-app/poja-api/commit/3caf931588e57eeafd6b736cb4bc0227b15a8302))
* revert 5f254e8ad13d9e1b9a523bd0e1f2a33385468374 and fa11e2a35c8bcf7f2bb863d2e8bc152e2dd084d8 ([1100fe4](https://github.com/poja-app/poja-api/commit/1100fe472e837320350bb67d729a78e8c7c37128))
* revert to BillingInfo model and use compute_datetime as last_computed_at ([f7f36c5](https://github.com/poja-app/poja-api/commit/f7f36c5ce273f201a3217d5cbb445fd4e89f261b))
* rm AppEnvDepl::isRedepl ([d1057dc](https://github.com/poja-app/poja-api/commit/d1057dc9443bf56a7c2b9592b5b48eb69d54926d))
* rm unused stack deletion and stack deployement endpoints ([1de59a1](https://github.com/poja-app/poja-api/commit/1de59a10793ba48ec473c99aebf169a895926ab2))
* save app env deployment ([4af299d](https://github.com/poja-app/poja-api/commit/4af299d767c0aa57930835bb18a0b09265c42c6c))
* save computed billed duration and computed price in billing info ([29f90df](https://github.com/poja-app/poja-api/commit/29f90df2aa024c4177e81fd90f0ee0025965ea89))
* save deployment progression history ([8a9a437](https://github.com/poja-app/poja-api/commit/8a9a437327a731ced5774dc332de962f839b9e88))
* save stack events for compute stack ([471546b](https://github.com/poja-app/poja-api/commit/471546b9a308e508ca2f19cf21baf3dbd1480d4f))
* save stack logs in s3 ([d97c8af](https://github.com/poja-app/poja-api/commit/d97c8af8427f4467a33196be61f81d20f84d082a))
* save stack outputs ([1ca3f27](https://github.com/poja-app/poja-api/commit/1ca3f27a4a22e1c06e6f8e69e7cef072c9068970))
* save stacks by env, type, and depl, allow filtering by depl ([ac13b1c](https://github.com/poja-app/poja-api/commit/ac13b1c5187dd5ca7dbd0bd652cd9b3cc7f3120b))
* save zero value in billing when query has no result ([7727bd7](https://github.com/poja-app/poja-api/commit/7727bd7a6a977f197ffa85821d2cbffd181809ff))
* separate env config and env deployment ([6d739b6](https://github.com/poja-app/poja-api/commit/6d739b6e6d5c0fbd04fce9438dd07fee70e93601))
* separate stripe code logic from payment request saving ([895f639](https://github.com/poja-app/poja-api/commit/895f6395b2c16c568e7e9702779cd4e5c1720020))
* set cli-version to 17.1.2 ([a8f0468](https://github.com/poja-app/poja-api/commit/a8f0468534cda8d554798605646ec6f14c6c33a2))
* simplify stack representation ([d7e7391](https://github.com/poja-app/poja-api/commit/d7e7391e61305edf26b5c63d19c25993e7103f31))
* specify can_be_invited_to_more_org in user component ([f34b0aa](https://github.com/poja-app/poja-api/commit/f34b0aa37b94d42d2e4d8392a957147e407dd858))
* specify can_invite_more_user in org component ([f29c774](https://github.com/poja-app/poja-api/commit/f29c7747a5a4e545ef063b57621cb3b264f40a37))
* subscriptions ([57b60e5](https://github.com/poja-app/poja-api/commit/57b60e57a6074ff798e5c96cf2f56cf5951f373e))
* tag name and tag message in BuiltEnvInfo, save them after initiating deployment ([e212f95](https://github.com/poja-app/poja-api/commit/e212f9540f7fe77ebbad28f200f3e62fd0eca415))
* timestamped billing info ([09cc3a4](https://github.com/poja-app/poja-api/commit/09cc3a4bf943d63c02f040e074421c8246d66c0e))
* **to-continue:** get stack list ([fe737eb](https://github.com/poja-app/poja-api/commit/fe737eb0d36ea7291abca2dd413312fe757f411d))
* update org users aws permissions ([12f7fc0](https://github.com/poja-app/poja-api/commit/12f7fc005bb00f90cf1ca474f32b530a91ca7d61))
* update poja public version and sam url ([2bc3cb6](https://github.com/poja-app/poja-api/commit/2bc3cb6d8f2034ac6868440cf12d53eafb790d73))
* update sam uri ([0a62ca2](https://github.com/poja-app/poja-api/commit/0a62ca2ce8afba3ff5bc9fe2920da1b30fb1e5a9))
* upload project package file ([75e661f](https://github.com/poja-app/poja-api/commit/75e661f83fd2934bf4dc608d1fe32fb2e805749d))
* use stackType instead of stackId on stack events ([910e257](https://github.com/poja-app/poja-api/commit/910e257a9c947a7adb2675163df8ee6e7eeb3bb0))
* user applications with user applications count ([e8d61cf](https://github.com/poja-app/poja-api/commit/e8d61cf0e21f7872d67361780ea3877562057d19))
* user must provide email adress during signup ([c02f97d](https://github.com/poja-app/poja-api/commit/c02f97db67c5ca5c4baf387a2ff55a4534db55fd))
* user sign up endpoint ([df971a6](https://github.com/poja-app/poja-api/commit/df971a688a7d024dc95a89f139d1c7efb0b1215d))
* user status reason ([b45c9a2](https://github.com/poja-app/poja-api/commit/b45c9a2a75a744dc0a5c4a67796ee12d285394ad))
* user with suspension duration and active sub ([71343d4](https://github.com/poja-app/poja-api/commit/71343d4b0ead6afb419d41f7aff99adf009367ab))
* User::status_updated_at ([ad5e926](https://github.com/poja-app/poja-api/commit/ad5e92670181a736869bfd9da3968373843cc05a))
* userStatus ACTIVE | SUSPENDED ([5620864](https://github.com/poja-app/poja-api/commit/5620864aea02c2ecc8e6530b7e0e6a9a46a7908c))
* whoami endpoint ([866b901](https://github.com/poja-app/poja-api/commit/866b90109b06670a6a5230c944df343fa8376e8a))


### Performance Improvements

* **adminBilling:** improve admin billing by paginating in sql ([40277f2](https://github.com/poja-app/poja-api/commit/40277f25cf428646b053876702a996ce315a3d20))
* get env by id, pre-load AppEnvDeployments ([5b6ed0b](https://github.com/poja-app/poja-api/commit/5b6ed0b4e8dd1b31c5031e5916537d3259b53edc))
* tune UserUpserted ([2cb1aaf](https://github.com/poja-app/poja-api/commit/2cb1aaf04d99d02054f307be751f3de650419804))
* use virtual threads ([04f88fb](https://github.com/poja-app/poja-api/commit/04f88fba207daa83534e4fee6d443f4d0e0fd1dd))
* worker2 events consumer duration real values ([380fffa](https://github.com/poja-app/poja-api/commit/380fffaf305c069114a64864c5443bfec83876df))


### Reverts

* Revert "feat: update org users aws permissions" ([91c9487](https://github.com/poja-app/poja-api/commit/91c9487083a0e299301b853f6dcb0afaf8c5e634))
* Revert "chore: compute end of day billing at LocalTime.MAX" ([5487dfa](https://github.com/poja-app/poja-api/commit/5487dfa51d8212b8eeca07dc2fe627218766b0ae))
* Revert "chore: update api url after compute stack is crupdated" ([8bd4a8d](https://github.com/poja-app/poja-api/commit/8bd4a8d47f3b9a30e076a279b9ad6f45ca04497b))
* Revert "chore: deploy compute after AppEnvDeployRequested backoff" ([bd678d8](https://github.com/poja-app/poja-api/commit/bd678d86e2752f04f8b7c2b4d540066487b55440))
* Revert "chore: fail appEnvDeployRequested on purpose for later consumption" ([e987e35](https://github.com/poja-app/poja-api/commit/e987e354f860530f797baec258385f9c64167e79))


### BREAKING CHANGES

* the whole project package structure has changed 

* feat: rename project to poja-api

* chore: update codeartifact information

* chore: update poja env vars ref

* chore: rename project packages
* move with_queues_nb from general to compute & drop
aurora related args
* drop support of previous versions below 1.2.0
* create cloud provider iam user per organization instead of per user
* use /user-stats instead of /users/stats
* use org_id instead of user_id to get app & env billing infos

* feat!: get app billing info by org id

* feat!: get app env billing info by org id

* chore: refactor code
* user /orgs/{orgId} instead of /users/{userId}

feat!: read env resources by org id

feat!: read & write log queries by org id
* use /orgs/{orgId} instead of /users/{userId}

feat!: write config by org id

chore: refactor code

feat!: read application deployment config by org id

feat!: write app env deployment with org id

feat!: read app deployments with org id

feat!: read app deployment states by org id

feat!: read app installations by org id

feat!: write app installations by org id

feat!: read app installation repositories by org id

feat!: read env stacks by org id

feat!: read stack events & outputs by org id

feat!: write stacks by org id
* use org_id instead of user_id to get app env resources
* use different types inheriting from BillingInfo to avoid null values on user's app's or env's billing info
* compute stack events retrieving starts when compute stack deployment is initiated but not anymore once it is completed.

* chore: establish deployment progression historization

* chore: refactor DeploymentStateEnum value

* chore: getCloudformationStackId returns Optional of String

* chore: get compute stack events during its deployment

* fix: ComputeStackCrupdateTriggeredServiceIT unit test
* in doc/api.yml add Environment[]  to Application




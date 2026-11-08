if (env.BRANCH_NAME == "master") {
    ciKubernetesDeploy {
        jobName = "siga-bot"
        junitFilePath = "/home/gradle/project/build/test-results/test/TEST-br.com.mariofronza.siga-bot.ApplicationTest.xml"
    }
} 

if (env.CHANGE_ID) {
    ciRunTests {
        jobName = "siga-bot"
        junitFilePath = "/home/gradle/project/build/test-results/test/TEST-br.com.mariofronza.siga-bot.ApplicationTest.xml"
    }
}

package com.example.visionsummerizer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
//import androidx.compose.foundation.layout.BoxScopeInstance.align
//import androidx.compose.foundation.layout.ColumnScopeInstance.align
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavGraph
import coil.compose.rememberImagePainter
import com.example.visionsummerizer.ml.T5SmallTp16
import com.example.visionsummerizer.presentations.CameraScreen
import com.example.visionsummerizer.ui.theme.VisionSummerizerTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.NavGraphSpec
import org.tensorflow.lite.DataType
import org.tensorflow.lite.examples.bertqa.ml.QaClient
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : ComponentActivity() {

    private var imageUri = mutableStateOf<Uri?>(null)
    private var textChanged = mutableStateOf("")
    private var summaryText = mutableStateOf("")
    private var questionAnswered = false
    private lateinit var handler: Handler
    private lateinit var qaClient: QaClient
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handlerThread = HandlerThread("QAClient")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        qaClient = QaClient(this)
        handler!!.post { qaClient!!.loadModel() }
        setContent {
            VisionSummerizerTheme {
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        MediumTopAppBar(
                            scrollBehavior = scrollBehavior,
                            title = {
                                Text(text = "VisionSummerizer")
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    },
                    content = { MainScreen(this, it) },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(

                            text = {
                                Text(text = "Add", color = MaterialTheme.colorScheme.primary)
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add",
                                )
                            },
                            onClick = { selectImage.launch("image/*") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    }
                )
            }
        }
    }

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri.value = uri
        }

    private fun shareText(sharedText: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharedText)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    @ExperimentalMaterial3Api
    @Composable
    fun MainScreen(context: Context, paddingValues: PaddingValues) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .height(500.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    if (imageUri.value != null) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = rememberImagePainter(
                                data = imageUri.value
                            ),
                            contentDescription = "image"
                        )

                    }

                    else{
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                        ){
                            Text(
                                text = "Drop Image to Summerize",
                            Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    Button(
                        modifier = Modifier
                            .align(Alignment.Center),
                        enabled = imageUri.value != null,
                        onClick = {
                            val image = InputImage.fromFilePath(context, imageUri.value!!)
                            recognizer.process(image)
                                .addOnSuccessListener {
                                    textChanged.value = it.text
                                    answerQuestion("Hello?")
                                }
                                .addOnFailureListener {
                                    Log.e("TEXT_REC", it.message.toString())
                                }
                        }) {
                        Icon(
                            Icons.Filled.Search,
                            "scan",
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Search",
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(12.dp),
                ){
                    Text(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        text = "Scanned Text",
                    )
                }
                Box(
                    modifier = Modifier.wrapContentSize()
                ){

                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        text = textChanged.value
                    )
                }
                Box(
                    modifier = Modifier.padding(12.dp)
                )
                {
                    var text by remember { mutableStateOf(TextFieldValue("")) }
                    Column() {


                        Box(modifier = Modifier.padding(8.dp)) {
                            OutlinedTextField(
                                value = text,
                                label = { Text(text = "Enter Your Question") },
                                onValueChange = {
                                    text = it
                                }
                            )
                        }

                        Box(modifier = Modifier.padding(8.dp)) {
                            Button(
                                modifier = Modifier.padding(5.dp),
                                shape = CutCornerShape(8.dp),
                                onClick = { answerQuestion(text.text) }
                            ) {
                                Text(text = "Find Me An Answer")
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(12.dp),
                ){
                    Text(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        text = "Answer",
                    )
                }
                Box(
                    modifier = Modifier.wrapContentSize()
                ){

                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        text = summaryText.value
                    )
                }

            }
            
        }
    }
    private fun answerQuestion(question: String) {
        var question = question
        question = question.trim { it <= ' ' }

        // Append question mark '?' if not ended with '?'.
        // This aligns with question format that trains the model.
        if (!question.endsWith("?")) {
            question += '?'
        }
        val questionToAsk = question
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        val focusView = currentFocus
        focusView?.clearFocus()

        questionAnswered = false

        // Run TF Lite model to get the answer.
        handler!!.post {
            val answers = qaClient!!.predict(questionToAsk, textChanged.value)
            if (!answers.isEmpty()) {
                // Get the top answer
                val topAnswer = answers[0]
                // Show the answer.
                runOnUiThread {
                    questionAnswered = true
                }
                Log.e("ANSWER", topAnswer.text.toString())
                summaryText.value = topAnswer.text.toString()
            }
        }
    }

}

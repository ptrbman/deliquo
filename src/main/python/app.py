# TODO:
# - Fix Select file to correctlt load data from CSV
# - Check if we need data from outside CSV file?

from collections import defaultdict
import os
import io
import dash
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output, State
import pandas as pd
import base64
from dash.exceptions import PreventUpdate
import dash_table

external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']
app = dash.Dash(__name__, external_stylesheets=external_stylesheets)

# Fetch all benchmarks
# And put into easy to use structure

currentfile = ""
solvers = "" # For now to make it global
results = ""
benchmarks = ""
data = ""
solverx = ""
solvery = ""
table = []

def parsecsv(df):
    global solvers
    global results
    global benchmarks
    global data
    global solverx
    global solvery
    global table
    benchmarks = []
    results = defaultdict(dict)
    solvers = df['solver'].unique()
    table = []

    for i, row in df.iterrows():
        bm = os.path.basename(row['benchmark'])
        benchmarks.append(bm)
        solver = row['solver']

        time = row['time']
        results[bm][solver] = time

    data = defaultdict(dict)
    for s in solvers:
        data[s] = []
    i = 0
    table = [dict() for x in range(len(results))]
    for k in results:
        table[i]['benchmark'] = k
        for s in solvers:
            table[i][s] = results[k][s]
            data[s].append(results[k][s])
        i += 1
    table = pd.DataFrame(table)

parsecsv(pd.read_csv('results.out'))


def benchmarkstr(bm):
    fstr = bm + "\n"
    for s in results[bm]:
        fstr = fstr + s + ": " + str(results[bm][s]) + "\n"
    return fstr

app.layout = html.Div([
    html.Div([
    dcc.Upload(
        id='upload-data',
        children=html.Div([
            'Drag and Drop or ',
            html.A('Select Files')
        ]),
        style={
            'width': '98%',
            'height': '60px',
            'lineHeight': '60px',
            'borderWidth': '1px',
            'borderStyle': 'dashed',
            'borderRadius': '5px',
            'textAlign': 'center',
            'margin': '10px'
        },
        # Allow multiple files to be uploaded
        multiple=False
    )]),
    html.Div([
        html.Div([
            dcc.Dropdown(
                id='xaxis-column',
                options=[{'label': i, 'value': i} for i in solvers],
                value=solvers[0]
            )
        ],
        style={'width': '48%', 'display': 'inline-block'}),

        html.Div([
            dcc.Dropdown(
                id='yaxis-column',
                options=[{'label': i, 'value': i} for i in solvers],
                value=solvers[1]
            )
        ],style={'width': '48%', 'display': 'inline-block'}),
    ]),

    dcc.Graph(id='indicator-graphic'),

    html.Div([
        dcc.Markdown("Benchmark Data (click point)"),
        html.Pre(id='click-data'),
    ]),

    html.Div(id='benchmark-graph', children = [dash_table.DataTable(id='datatable')]),
    html.Div(id='bmlist')
])


@app.callback(Output('bmlist', 'children'),
              [Input('datatable', 'selected_rows')],
              [State('datatable', 'data')])
def buttonclick(selected_rows, data):
    if selected_rows != None:
        resultingStuff = []
        for r in selected_rows:
            resultingStuff.append(data[r]['benchmark'])
            resultingStuff.append(html.Br())
        return resultingStuff
    return "No selected"

def update_table():
    return [
        dash_table.DataTable(
            id='datatable',
            columns = [{"name" : i, "id" : i,} for i in table.columns],
            data=table.to_dict('records'),
            filter_action="native",
            sort_action="native",
            sort_mode="multi",
            row_selectable="multi",
            page_action="native",
            page_size= 200,
        )]

def update_graph():
    return {
        'data': [dict(
            x=data[solverx],
            y=data[solvery],
            text=benchmarks,
            mode='markers',
            marker={
                'size': 15,
                'opacity': 0.5,
                'line': {'width': 0.5, 'color': 'white'}
            }
        )],
        'layout': dict(
            xaxis={
                'title': solverx,
                'type': 'linear' 
            },
            yaxis={
                'title': solvery,
                'type': 'linear' 
            },
            margin={'l': 40, 'b': 40, 't': 10, 'r': 0},
            hovermode='closest'
        )
    }



@app.callback(
    [Output('indicator-graphic', 'figure'),
    Output('benchmark-graph', 'children')],
    [Input('xaxis-column', 'value'),
     Input('yaxis-column', 'value'),
     Input('upload-data', 'contents')],
     [State('upload-data', 'filename')])
def change_solvers(xaxis_column_name, yaxis_column_name, contents, filename):
    global solverx
    global solvery
    print("change_solvers(", xaxis_column_name, ", ", yaxis_column_name, ", data, ", filename, ")")
    solverx = xaxis_column_name
    solvery = yaxis_column_name

    if (filename != None):
        try:
            content_type, content_string = contents.split(',')
            # Assume that the user uploaded a CSV file
            decoded = base64.b64decode(content_string)
            df = pd.read_csv(io.StringIO(decoded.decode('utf-8')))
            parsecsv(df)
        except Exception as e:
            print(e)
            return html.Div([
                'There was an error processing this file.'
            ])

    return update_graph(),update_table()

@app.callback(
    Output('click-data', 'children'),
    [Input('indicator-graphic', 'clickData')])
def display_click_data(clickData):
    if (clickData != None):
        return benchmarkstr(clickData['points'][0]['text'])


if __name__ == '__main__':
    app.run_server(debug=True)
